package org.bruchez.olivier.msatozip

import java.io.DataInputStream

object DataInputStreamHelper {
  implicit class DataInputStreamOps(is: DataInputStream) {
    def readUnsignedShortLittleEndian(): Int = {
      val ch1 = is.readByte().toInt & 0xff
      val ch2 = is.readByte().toInt & 0xff

      (ch2 << 8) + (ch1 << 0)
    }

    def readUnsigned24LittleEndian(): Int = {
      val ch1 = is.readByte().toInt & 0xff
      val ch2 = is.readByte().toInt & 0xff
      val ch3 = is.readByte().toInt & 0xff

      (ch3 << 16) + (ch2 << 8) + (ch1 << 0)
    }

    def readUnsignedIntLittleEndian(): Long = {
      val ch1 = is.readByte().toLong & 0xff
      val ch2 = is.readByte().toLong & 0xff
      val ch3 = is.readByte().toLong & 0xff
      val ch4 = is.readByte().toLong & 0xff

      (ch4 << 24) + (ch3 << 16) + (ch2 << 8) + (ch1 << 0)
    }
  }
}
