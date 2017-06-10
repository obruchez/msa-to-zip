package org.bruchez.olivier.msatozip.msa

case class Sector(data: Array[Byte])

object Sector {
  val Length = 512
}
