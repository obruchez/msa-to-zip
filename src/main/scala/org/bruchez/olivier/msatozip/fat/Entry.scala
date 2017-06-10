package org.bruchez.olivier.msatozip.fat

import org.bruchez.olivier.msatozip.DataInputStreamHelper._
import java.io.DataInputStream
import java.nio.charset.StandardCharsets
import java.time.{ LocalDate, LocalTime }

sealed trait Entry

case class UsedEntry(
  name: String,
  extension: String,
  attributes: Attributes,
  time: Option[LocalTime],
  date: Option[LocalDate],
  startingCluster: Int,
  size: Int
) extends Entry

case object FreeEntry extends Entry

case object DeletedEntry extends Entry

object Entry {
  val Length = 32

  def apply(is: DataInputStream): Entry = {
    val firstByte = is.readByte().toInt & 0xff

    if (firstByte == 0x00) {
      is.skipBytes(Length - 1)
      FreeEntry
    } else if (firstByte == 0xe5) {
      is.skipBytes(Length - 1)
      DeletedEntry
    } else {
      val ActualCharacterFor05 = 0xe5
      val translatedFirstByte = if (firstByte == 0x05) ActualCharacterFor05 else firstByte
      UsedEntry(firstByte = translatedFirstByte.toByte, is)
    }
  }
}

object UsedEntry {
  // scalastyle:off method.length
  def apply(firstByte: Byte, is: DataInputStream): UsedEntry = {
    // FNAME
    val nameBytes = new Array[Byte](NameLength)
    nameBytes(0) = firstByte
    is.read(nameBytes, 1, NameLength - 1)
    val name = stringFromBytes(nameBytes).trim

    // FEXT
    val extensionBytes = new Array[Byte](ExtensionLength)
    is.read(extensionBytes, 0, ExtensionLength)
    val extension = stringFromBytes(extensionBytes).trim

    // ATTRIB
    val attributes = Attributes(is)

    // RES
    is.skipBytes(ReservedByteCount)

    // FTIME
    val rawTime = is.readUnsignedShortLittleEndian()
    val time = timeOption(rawTime)

    // FDATE
    val rawDate = is.readUnsignedShortLittleEndian()
    val date = dateOption(rawDate)

    // SCLUSTER
    val startingCluster = is.readUnsignedShortLittleEndian()

    // FSIZE
    val size = is.readUnsignedIntLittleEndian().toInt

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
  // scalastyle:on method.length

  private def timeOption(value: Int): Option[LocalTime] = {
    val hours = (value >> 11) & 0x1f
    val minutes = (value >> 5) & 0x3f
    val seconds = ((value >> 0) & 0x1f) * 2
    if (hours >= 0 && hours <= 23 && minutes >= 0 && minutes <= 59 && seconds >= 0 && seconds <= 59) {
      Some(LocalTime.of(hours, minutes, seconds))
    } else {
      None
    }
  }

  private def dateOption(value: Int): Option[LocalDate] = {
    val year = ((value >> 9) & 0x7f) + 1980
    val month = (value >> 5) & 0xf
    val day = (value >> 0) & 0x1f
    if (day >= 1 && day <= 31 && month >= 1 && month <= 12 && year >= 1980) {
      Some(LocalDate.of(year, month, day))
    } else {
      None
    }
  }

  private val NameLength = 8
  private val ExtensionLength = 3
  private val ReservedByteCount = 10

  private def stringFromBytes(bytes: Array[Byte]): String = {
    // @todo actually translate from Atari charset
    new String(bytes, StandardCharsets.US_ASCII)
  }
}
