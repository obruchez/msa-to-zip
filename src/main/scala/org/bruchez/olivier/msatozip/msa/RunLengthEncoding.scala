package org.bruchez.olivier.msatozip.msa

import java.io.DataInputStream

import scala.collection.mutable.ArrayBuffer

object RunLengthEncoding {
  // Naive and non-optimized decoder
  def decode(dis: DataInputStream, bytesToRead: Int): Array[Byte] = {
    val buffer = new ArrayBuffer[Byte]()

    var bytesLeftToRead = bytesToRead

    while (bytesLeftToRead > 0) {
      val byte = dis.readByte()

      if (byte != RunLengthByte) {
        buffer.append(byte)
        bytesLeftToRead -= 1
      } else {
        val dataByte = dis.readByte()
        val runLength = dis.readUnsignedShort()

        for (i <- 1 to runLength) {
          buffer.append(dataByte)
        }

        bytesLeftToRead -= 4
      }
    }

    buffer.toArray
  }

  private val RunLengthByte = 0xe5.toByte
}
