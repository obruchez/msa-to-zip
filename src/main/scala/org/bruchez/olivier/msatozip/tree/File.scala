package org.bruchez.olivier.msatozip.tree

import java.time.LocalDateTime

import org.bruchez.olivier.msatozip.fat.Corruption

sealed trait DirectoryOrFile {
  def name: String
  def dateTime: Option[LocalDateTime]
}

case class Directory(
  override val name: String,
  override val dateTime: Option[LocalDateTime],
  children: Seq[DirectoryOrFile]
) extends DirectoryOrFile

case class File(
  override val name: String,
  override val dateTime: Option[LocalDateTime],
  data: Seq[Byte]
) extends DirectoryOrFile

case class CorruptedFile(
  override val name: String,
  override val dateTime: Option[LocalDateTime],
  data: Seq[Byte],
  corruption: Corruption
) extends DirectoryOrFile
