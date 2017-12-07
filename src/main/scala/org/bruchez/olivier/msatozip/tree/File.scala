package org.bruchez.olivier.msatozip.tree

import java.nio.file._
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import org.bruchez.olivier.msatozip.fat.Corruption

sealed trait DirectoryOrFile {
  def name: String
  def dateTime: Option[LocalDateTime]
  def logs(level: Int): Seq[String]

  def saveLogs(path: Path): Unit =
    Files.write(
      path,
      logs(level = 0).mkString("\n").getBytes("utf-8"),
      StandardOpenOption.CREATE,
      StandardOpenOption.TRUNCATE_EXISTING
    )

  protected def logLine(level: Int, sizeOption: Option[Int]): String = {
    val dateTimeString = dateTime.map(formatter.format).getOrElse(NoDateTimeString)
    val sizeString = sizeOption match {
      case Some(size) =>
        val MaxRawSizeString = 7
        val rawSizeString = size.toString
        " " * (MaxRawSizeString - rawSizeString.size) + rawSizeString
      case None =>
        ""
    }
    val nameString = s" $name"

    ("  " * level) + dateTimeString + sizeString + nameString
  }

  private val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
  private val NoDateTimeString = "????-??-?? ??:??:??"
}

case class Directory(
    override val name: String,
    override val dateTime: Option[LocalDateTime],
    children: Seq[DirectoryOrFile]
) extends DirectoryOrFile {
  override def logs(level: Int): Seq[String] = {
    val rootDirectory = name.isEmpty

    val currentDirectoryLogs = if (rootDirectory) Seq() else Seq(logLine(level, sizeOption = None))
    val childrenLevel = if (rootDirectory) level else level + 1
    val childrenLogs = children.flatMap(_.logs(childrenLevel))

    currentDirectoryLogs ++ childrenLogs
  }
}

case class File(
    override val name: String,
    override val dateTime: Option[LocalDateTime],
    data: Seq[Byte]
) extends DirectoryOrFile {
  override def logs(level: Int): Seq[String] = Seq(logLine(level, sizeOption = Some(data.size)))
}

case class CorruptedFile(
    override val name: String,
    override val dateTime: Option[LocalDateTime],
    data: Seq[Byte],
    corruption: Corruption
) extends DirectoryOrFile {
  override def logs(level: Int): Seq[String] =
    Seq(logLine(level, sizeOption = Some(data.size)) + " (corrupted)")
}
