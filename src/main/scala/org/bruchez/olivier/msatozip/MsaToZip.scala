package org.bruchez.olivier.msatozip

import java.nio.file.Paths

object MsaToZip {
  def main(args: Array[String]): Unit = {
    val msaImage = MsaImage(Paths.get("/Users/olivierbruchez/Downloads/olivier1.msa"))
    println(s"msaImage = $msaImage")
  }
}
