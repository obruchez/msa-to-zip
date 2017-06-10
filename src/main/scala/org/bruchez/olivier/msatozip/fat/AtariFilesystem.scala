package org.bruchez.olivier.msatozip.fat

import java.io.{ ByteArrayInputStream, DataInputStream }
import java.time.LocalDateTime

import org.bruchez.olivier.msatozip.msa.MsaImage
import org.bruchez.olivier.msatozip.tree._

class AtariFilesystem(msaImage: MsaImage) {
  def fileTree(): Directory = {
    def directory(name: String, dateTime: Option[LocalDateTime], entries: Entries): Directory = {
      val directoriesAndFiles = (for {
        usedEntry <- entries.entries.collect { case usedEntry: UsedEntry => usedEntry }
      } yield {
        val fullName = usedEntry.name + Option(usedEntry.extension).filter(_.nonEmpty).map("." + _).getOrElse("")

        val dateTime =
          for {
            date <- usedEntry.date
            time <- usedEntry.time
          } yield LocalDateTime.of(date, time)

        if (usedEntry.attributes.directory) {
          if (!Set(".", "..").contains(usedEntry.name)) {
            Some(directory(fullName, dateTime, directorySubEntries(usedEntry)))
          } else {
            None
          }
        } else {
          Some(File(fullName, dateTime, data = fileData(usedEntry)))
        }
      }).flatten

      val directories = directoriesAndFiles collect { case directory: Directory => directory } sortBy (_.name)
      val files = directoriesAndFiles collect { case file: File => file } sortBy (_.name)

      Directory(name, dateTime, directories ++ files)
    }

    directory(name = "", dateTime = None, rootEntries)
  }

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
