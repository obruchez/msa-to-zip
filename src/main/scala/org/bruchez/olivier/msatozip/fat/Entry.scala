package org.bruchez.olivier.msatozip.fat

import java.io.DataInputStream
import java.nio.charset.StandardCharsets
import java.time.{ LocalDate, LocalTime }

sealed trait Entry

case class UsedEntry(
  name: String,
  extension: String,
  attributes: Attributes,
  time: LocalTime,
  date: LocalDate,
  startingCluster: Int,
  size: Int
) extends Entry

case object FreeEntry extends Entry

case object DeletedEntry extends Entry

object Entry {
  def apply(is: DataInputStream): Entry = {
    val RemainingBytesToSkip = 31

    val firstByte = is.readByte().toInt & 0xff

    if (firstByte == 0x00) {
      is.skipBytes(RemainingBytesToSkip)
      FreeEntry
    } else if (firstByte == 0xe5) {
      is.skipBytes(RemainingBytesToSkip)
      DeletedEntry
    } else {
      val ActualCharacterFor05 = 0xe5
      val translatedFirstByte = if (firstByte == 0x05) ActualCharacterFor05 else firstByte
      UsedEntry(firstByte = translatedFirstByte.toByte, is)
    }
  }
}

object UsedEntry {
  def apply(firstByte: Byte, is: DataInputStream): UsedEntry = {
    // FNAME
    val nameBytes = new Array[Byte](NameLength)
    nameBytes(0) = firstByte
    is.read(nameBytes, 1, NameLength - 1)
    val name = stringFromBytes(nameBytes)

    // FEXT
    val extensionBytes = new Array[Byte](ExtensionLength)
    is.read(extensionBytes)
    val extension = stringFromBytes(extensionBytes)

    // ATTRIB
    val attributes = Attributes(is)

    // RES
    is.skipBytes(ReservedByteCount)

    // FTIME
    val rawTime = is.readUnsignedShort()
    val hours = (rawTime >> 11) & 0x1f
    val minutes = (rawTime >> 5) & 0x3f
    val seconds = ((rawTime >> 0) & 0x1f) * 2
    val time = LocalTime.of(hours, minutes, seconds)

    // FDATE
    val rawDate = is.readUnsignedShort()
    val year = (rawDate >> 9) & 0x7f + 1980
    val month = (rawDate >> 5) & 0xf
    val day = (rawDate >> 0) & 0x1f
    val date = LocalDate.of(year, month, day)

    // SCLUSTER
    val startingCluster = is.readUnsignedShort()

    // FSIZE
    val size = is.readInt()

    UsedEntry(
      name = name,
      extension = extension,
      attributes = attributes,
      time = time,
      date = date,
      startingCluster = startingCluster,
      size = size
    )
  }

  private val NameLength = 8
  private val ExtensionLength = 3
  private val ReservedByteCount = 10

  private def stringFromBytes(bytes: Array[Byte]): String = {
    // @todo actually translate from Atari charset
    new String(bytes, StandardCharsets.US_ASCII)
  }
}
