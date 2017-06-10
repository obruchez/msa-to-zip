package org.bruchez.olivier.msatozip.fat

import org.bruchez.olivier.msatozip.DataInputStreamHelper._
import java.io.DataInputStream

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

object BootSector {
  // scalastyle:off method.length
  def apply(is: DataInputStream): BootSector = {
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
  // scalastyle:on method.length
}
