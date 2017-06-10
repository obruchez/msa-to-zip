package org.bruchez.olivier.msatozip.tree

import java.time.LocalDateTime

sealed trait DirectoryOrFile {
  def name: String
  def dateTime: Option[LocalDateTime]
}

case class File(
  override val name: String,
  override val dateTime: Option[LocalDateTime],
  data: Seq[Byte]
) extends DirectoryOrFile

case class Directory(
  override val name: String,
  override val dateTime: Option[LocalDateTime],
  children: Seq[DirectoryOrFile]
) extends DirectoryOrFile
