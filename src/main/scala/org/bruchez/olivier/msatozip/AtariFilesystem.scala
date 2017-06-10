package org.bruchez.olivier.msatozip

import java.io.{ ByteArrayInputStream, DataInputStream }

class AtariFilesystem(msaImage: MsaImage) {
  import DataInputStreamHelper._

  private def withData[T](offset: Int)(f: DataInputStream => T): T = {
    val is = new DataInputStream(new ByteArrayInputStream(msaImage.data.drop(offset)))

    try {
      f(is)
    } finally {
      is.close()
    }
  }

  lazy val bootSector: BootSector = withData(offset = 0) { is =>
    // BRA
    is.skipBytes(2)

    // OEM
    is.skipBytes(6)

    // SERIAL
    val serialNumber = is.readUnsigned24LittleEndian()

    // BPS
    val bytesPerSector = is.readUnsignedShortLittleEndian()

    // SPC
    val sectorsPerCluster = is.readUnsignedByte()

    // RESSEC
    val reservedSectorCount = is.readUnsignedShortLittleEndian()

    // NFATS
    val fatCount = is.readUnsignedByte()

    // NDIRS
    val rootDirectoryEntryCount = is.readUnsignedShortLittleEndian()

    // NSECTS
    val sectorCount = is.readUnsignedShortLittleEndian()

    // MEDIA
    is.skipBytes(1)

    // SPF
    val fatSizeInSectors = is.readUnsignedShortLittleEndian()

    // SPT
    val sectorsPerTrack = is.readUnsignedShortLittleEndian()

    // NHEADS
    val sideCount = is.readUnsignedShortLittleEndian()

    BootSector(
      serialNumber = serialNumber,
      bytesPerSector = bytesPerSector,
      sectorsPerCluster = sectorsPerCluster,
      reservedSectorCount = reservedSectorCount,
      fatCount = fatCount,
      rootDirectoryEntryCount = rootDirectoryEntryCount,
      sectorCount = sectorCount,
      fatSizeInSectors = fatSizeInSectors,
      sectorsPerTrack = sectorsPerTrack,
      sideCount = sideCount
    )
  }
}

object AtariFilesystem {

}

case class BootSector(
    serialNumber: Int,
    bytesPerSector: Int,
    sectorsPerCluster: Int,
    reservedSectorCount: Int,
    fatCount: Int,
    rootDirectoryEntryCount: Int,
    sectorCount: Int,
    fatSizeInSectors: Int,
    sectorsPerTrack: Int,
    sideCount: Int
) {
  def print(): Unit = {
    println(s"serialNumber = $serialNumber")
    println(s"bytesPerSector = $bytesPerSector")
    println(s"sectorsPerCluster = $sectorsPerCluster")
    println(s"reservedSectorCount = $reservedSectorCount")
    println(s"fatCount = $fatCount")
    println(s"rootDirectoryEntryCount = $rootDirectoryEntryCount")
    println(s"sectorCount = $sectorCount")
    println(s"fatSizeInSectors = $fatSizeInSectors")
    println(s"sectorsPerTrack = $sectorsPerTrack")
    println(s"sideCount = $sideCount")
  }
}

case class Fat()

case class Entry()
