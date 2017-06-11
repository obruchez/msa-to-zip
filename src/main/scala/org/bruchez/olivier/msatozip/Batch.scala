package org.bruchez.olivier.msatozip

import java.nio.file._

import org.bruchez.olivier.msatozip.fat.AtariFilesystem
import org.bruchez.olivier.msatozip.msa.MsaImage

import scala.collection.JavaConverters._
import scala.util._

object Batch {
  def convertToZip(directory: Path): Unit = {
    for {
      path <- Files.walk(directory).iterator().asScala
      if Files.isRegularFile(path)
      if path.toString.endsWith(".msa")
    } {
      println(path)
      val msaImage = MsaImage(path)
      val atariFilesystem = new AtariFilesystem(msaImage)
      val fileTreeTry = Try(atariFilesystem.fileTree())
      fileTreeTry match {
        case Success(_) =>
          println(" -> SUCCESS")
        case Failure(throwable) =>
          println(s" -> FAILURE (${throwable.getMessage})")
      }
    }
  }
}
