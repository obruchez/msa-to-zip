package org.bruchez.olivier.msatozip.fat

import java.io.{ ByteArrayInputStream, DataInputStream }
import java.time.LocalDateTime

import org.bruchez.olivier.msatozip.msa.MsaImage
import org.bruchez.olivier.msatozip.tree._

import scala.util._

/*
Based on following documentation:

- http://info-coach.fr/atari/software/FD-Soft.php
- https://en.wikipedia.org/wiki/Design_of_the_FAT_file_system
- http://forensicswiki.org/wiki/FAT
 */

class AtariFilesystem(msaImage: MsaImage) {
  // scalastyle:off cyclomatic.complexity
  def fileTree(): Directory = {
    def directory(name: String, dateTime: Option[LocalDateTime], entries: Entries): Directory = {
      val usedEntries = entries.entries.collect { case usedEntry: UsedEntry => usedEntry }

      val directoriesAndFiles = usedEntries flatMap { usedEntry =>
        if (usedEntry.name == "." || usedEntry.name == "..") {
          // Ignore current/parent directories
          None
        } else if (usedEntry.attributes.volumeLabel || usedEntry.startingCluster < 2 || usedEntry.size < 0) {
          // Silently skip abnormal entries
          None
        } else if (usedEntry.attributes.directory) {
          // Directory => retrieve sub-entries
          Some(directory(usedEntry.filename, usedEntry.dateTime, directorySubEntries(usedEntry)))
        } else if (usedEntry.size == 0) {
          // Empty file => do not examine starting cluster
          Some(File(usedEntry.filename, usedEntry.dateTime, data = Seq()))
        } else {
          Try(fileData(usedEntry)) match {
            case Failure(ce: ClusterException) =>
              //println(s"***** ${usedEntry.filename} -> size ${usedEntry.size}, cluster = ${usedEntry.startingCluster} -> ${ce.getMessage}")
              //usedEntry.attributes.print()

              // @todo return data before corrupted cluster
              Some(CorruptedFile(usedEntry.filename, usedEntry.dateTime, data = Seq(), corruption = ce.corruption))
            case Failure(throwable) =>
              throw throwable
            case Success(data) =>
              Some(File(usedEntry.filename, usedEntry.dateTime, data = data))
          }
        }
      }

      val directories = directoriesAndFiles collect { case directory: Directory => directory } sortBy (_.name)
      val files = directoriesAndFiles collect { case file: File => file } sortBy (_.name)

      Directory(name, dateTime, directories ++ files)
    }

    directory(name = "", dateTime = None, rootEntries)
  }
  // scalastyle:on cyclomatic.complexity

  private def clusterData(cluster: Int): Seq[Byte] =
    withData(offset = offsetFromCluster(cluster)) { is =>
      val buffer = new Array[Byte](bootSector.sectorsPerCluster * bootSector.bytesPerSector)
      is.read(buffer)
      buffer
    }

  private def fileData(fileEntry: UsedEntry): Seq[Byte] = {
    assert(!fileEntry.attributes.directory, s"Cannot list sub-entries for non-directory entry")

    val bytes = fat.clustersFromStartingCluster(fileEntry.startingCluster).flatMap(clusterData)

    bytes.take(fileEntry.size)
  }

  private def directorySubEntries(directoryEntry: UsedEntry): Entries = {
    assert(directoryEntry.attributes.directory, s"Cannot list sub-entries for non-directory entry")

    val offset = offsetFromCluster(directoryEntry.startingCluster)

    withData(offset = offset)(Entries(_, bootSector.normalDirectoryEntryCount))
  }

  private def withData[T](offset: Int)(f: DataInputStream => T): T = {
    val is = new DataInputStream(new ByteArrayInputStream(msaImage.data.drop(offset)))

    try {
      f(is)
    } finally {
      is.close()
    }
  }

  private def offsetFromCluster(cluster: Int): Int = {
    // Cluster numbering actually starts at 2 for FAT12/16
    (cluster - 2) * bootSector.sectorsPerCluster * bootSector.bytesPerSector + bootSector.userDataOffset
  }

  lazy val bootSector: BootSector =
    withData(offset = 0)(BootSector(_))

  lazy val fat: Fat =
    withData(offset = bootSector.fatOffset)(Fat(_, bootSector.clustersByFat))

  lazy val rootEntries: Entries =
    withData(offset = bootSector.rootDirectoryOffset)(Entries(_, bootSector.rootDirectoryEntryCount))
}
