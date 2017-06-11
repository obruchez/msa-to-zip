package org.bruchez.olivier.msatozip.fat

import java.io.DataInputStream

case class Attributes(
    readOnly: Boolean,
    hidden: Boolean,
    system: Boolean,
    volumeLabel: Boolean,
    directory: Boolean,
    newOrModified: Boolean
) {
  def print(): Unit = {
    println(s"readOnly = $readOnly")
    println(s"hidden = $hidden")
    println(s"system = $system")
    println(s"volumeLabel = $volumeLabel")
    println(s"directory = $directory")
    println(s"newOrModified = $newOrModified")
  }
}

object Attributes {
  def apply(is: DataInputStream): Attributes = {
    val byte = is.readByte()

    Attributes(
      readOnly = (byte & 0x01) != 0,
      hidden = (byte & 0x02) != 0,
      system = (byte & 0x04) != 0,
      volumeLabel = (byte & 0x08) != 0,
      directory = (byte & 0x10) != 0,
      newOrModified = (byte & 0x20) != 0
    )
  }
}
