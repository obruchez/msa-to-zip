package org.bruchez.olivier.msatozip.msa

import java.io.DataInputStream
import java.nio.file._

import scala.collection.mutable.ArrayBuffer

case class MsaImage(
    sectorLength: Int,
    sectorsPerTrack: Int,
    startTrack: Int,
    endTrack: Int,
    sides: Seq[Side]
) {
  lazy val data: Array[Byte] = {
    val buffer = new ArrayBuffer[Byte]()

    for {
      track <- startTrack to endTrack
      side <- sides.indices
      sector <- 0 until sectorsPerTrack
    } {
      buffer.append(sides(side).tracks(track - startTrack).sectors(sector).data: _*)
    }

    buffer.toArray
  }

  lazy val totalLength: Long = sides.length * (endTrack - startTrack + 1) * sectorsPerTrack * sectorLength
}

object MsaImage {
  def apply(file: Path): MsaImage = {
    val dis = new DataInputStream(Files.newInputStream(file))

    try {
      val signature = dis.readUnsignedShort()
      assert(signature == MsaSignature, f"Unexpected header signature: $signature%X")

      val sectorsPerTrack = dis.readUnsignedShort()

      val sideCount = dis.readUnsignedShort() + 1
      assert(sideCount == 1 || sideCount == 2, s"Unexpected side count: $sideCount")

      val startTrack = dis.readUnsignedShort()

      val endTrack = dis.readUnsignedShort()

      val tracksBySideAndTrack =
        (for {
          track <- startTrack to endTrack
          side <- 0 until sideCount
        } yield {
          (side, track) -> Track(dis, sectorsPerTrack)
        }).toMap

      val sides =
        for (side <- 0 until sideCount) yield {
          Side(tracks = for (track <- startTrack to endTrack) yield tracksBySideAndTrack((side, track)))
        }

      MsaImage(
        sectorLength = Sector.Length,
        sectorsPerTrack = sectorsPerTrack,
        startTrack = startTrack,
        endTrack = endTrack,
        sides = sides
      )
    } finally {
      dis.close()
    }
  }

  private val MsaSignature = 0x0E0F
}

/*
http://info-coach.fr/atari/documents/_mydoc/FD_Image_File_Format.pdf

MSA FILE FORMAT
The .MSA image file format is made up as follows:
Header:
 Word ID marker, should be $0E0F
 Word Sectors per track
 Word Sides (0 or 1; add 1 to this to get correct number of sides)
 Word Starting track (0-based)
 Word Ending track (0-based)
Individual tracks follow the header in alternating side order, e.g. a double sided disk is stored as:
 TRACK 0, SIDE 0
 TRACK 0, SIDE 1
 TRACK 1, SIDE 0
 TRACK 1, SIDE 1
 TRACK 2, SIDE 0
 TRACK 2, SIDE 1
 ...
Track blocks are made up as follows:
 Word Data length
 Bytes Data
If the data length is equal to 512 x the sectors per track value, it is an uncompressed track and you
can merely copy the data to the appropriate track of the disk. However, if the data length value is
less than 512 x the sectors per track value it is a compressed track.
Compressed tracks use simple a Run Length Encoding (RLE) compression method. You can directly
copy any data bytes until you find an $E5 byte. This signals a compressed run, and is made up as
follows:
 Byte Marker - $E5
 Byte Data byte
 Word Run length
So, if MSA found six $AA bytes in a row it would encode it as: $E5AA0006
What happens if there's an actual $E5 byte on the disk? Well, logically enough, it is encoded as:
$E5E50001
This is obviously bad news if a disk consists of lots of data like $E500E500E500E500... but if MSA
makes a track bigger when attempting to compress it, it just stores the uncompressed version
instead.
MSA only compresses runs of at least 4 identical bytes (after all, it would be wasteful to store 4 bytes
for a run of only 3 identical bytes!). There is one exception to this rule: if a run of 2 or 3 $E5 bytes is
found, that is stored appropriately enough as a run. Again, it would be wasteful to store 4 bytes for
every single $E5 byte.
The hacked release of MSA that enables the user to turn off compression completely simply stops
MSA from trying this compression and produces MSA images that are completely uncompressed.
This is okay because it is possible for MSA to produce such an image anyway, and such images are
therefore 100% compatible with normal MSA versions (and MSA-to-ST of course).
 */
