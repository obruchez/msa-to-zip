package org.bruchez.olivier.msatozip.msa

import java.io.DataInputStream

case class Track(sectors: Seq[Sector])

object Track {
  def apply(dis: DataInputStream, sectorsPerTrack: Int): Track = {
    val dataLength = dis.readUnsignedShort()

    val expectedLength = Sector.Length * sectorsPerTrack

    val trackData =
      if (dataLength != expectedLength) {
        // Compressed data
        val decodedData = RunLengthEncoding.decode(dis, dataLength)

        assert(
          decodedData.length == expectedLength,
          s"Decoded track has unexpected size (${decodedData.length} bytes vs $expectedLength bytes)")

        decodedData
      } else {
        // Uncompressed data
        val trackData = new Array[Byte](dataLength)
        val readByteCount = dis.read(trackData)

        assert(readByteCount == dataLength, s"Could not read entire track ($dataLength bytes)")

        trackData
      }

    val sectors =
      for (sector <- 0 until sectorsPerTrack) yield {
        val offset = sector * Sector.Length
        Sector(data = trackData.slice(offset, offset + Sector.Length))
      }

    Track(sectors = sectors)
  }
}
