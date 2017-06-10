package org.bruchez.olivier.msatozip

import java.nio.ByteBuffer
import java.nio.file.Paths

import de.waldheinz.fs.fat.FatFileSystem
import org.bruchez.olivier.msatozip.fat.AtariFilesystem
import org.bruchez.olivier.msatozip.msa.MsaImage

import scala.collection.JavaConverters._

object MsaToZip {
  // scalastyle:off method.length
  def main(args: Array[String]): Unit = {
    //val msaImage = MsaImage(Paths.get("/Users/olivierbruchez/Downloads/atari37.msa"))
    val msaImage = MsaImage(Paths.get("/Users/olivierbruchez/Downloads/olivier1.msa"))

    val atariFilesystem = new AtariFilesystem(msaImage)

    val bootSector = atariFilesystem.bootSector
    bootSector.print()

    val fat = atariFilesystem.fat
    //println(s"fat = $fat")

    /*for (startingCluster <- 0 until bootSector.clustersByFat) {
      val clusters = fat.clustersFromStartingCluster(startingCluster)
      println(s"clusters($startingCluster) = $clusters")
    }*/

    println(s"fat -> ${fat.clusters.size} clusters")

    val firstEntry = atariFilesystem.firstEntry
    println(s"firstEntry = $firstEntry")

    System.exit(0)

    /*val bootSector = msaImage.sides.head.tracks.head.sectors.head
    bootSector.data(510) = 0x55.toByte
    bootSector.data(511) = 0xaa.toByte*/

    /*for (i <- 0 until 512) {
      print(f"${data(i)}%02X ")
    }

    for (i <- 0 until msaImage.data.length - 1) {
      if (msaImage.data(i) == 0x55.toByte && msaImage.data(i + 1) == 0xaa.toByte) {
        println(s"BOOT SIG at $i")
      }
    }*/

    val fs = FatFileSystem.read(new MsaBlockDevice(msaImage), /*readOnly*/ true)

    val directory = fs.getRoot

    for {
      entry <- directory.iterator().asScala.toSeq
    } {
      if (!entry.isDirectory) {
        val file = entry.getFile
        val fileLength = file.getLength.toInt

        println(s"entry = ${entry.getName} (length = $fileLength)")

        val buffer = ByteBuffer.allocate(fileLength)
        file.read(0, buffer)

        //val string = new String(buffer.array, "ISO-8859-1")
        //println(s"string = $string")
      }
    }
  }
  // scalastyle:on method.length
}
