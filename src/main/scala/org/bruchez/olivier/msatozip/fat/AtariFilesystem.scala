package org.bruchez.olivier.msatozip.fat

import java.io.{ ByteArrayInputStream, DataInputStream }

import org.bruchez.olivier.msatozip.msa.MsaImage

class AtariFilesystem(msaImage: MsaImage) {
  def clusterData(cluster: Int): Array[Byte] =
    withData(offset = offsetFromCluster(cluster)) { is =>
      val buffer = new Array[Byte](bootSector.sectorsPerCluster * bootSector.bytesPerSector)
      is.read(buffer)
      buffer
    }

  def fileData(fileEntry: UsedEntry): Seq[Byte] = {
    assert(!fileEntry.attributes.directory, s"Cannot list sub-entries for non-directory entry")

    val bytes = fat.clustersFromStartingCluster(fileEntry.startingCluster).flatMap(clusterData)

    bytes.take(fileEntry.size)
  }

  def directorySubEntries(directoryEntry: UsedEntry): Entries = {
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
