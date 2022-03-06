package org.bruchez.olivier.msatozip.zip

import java.net.URI
import java.nio.file._
import java.nio.file.attribute.FileTime
import java.time.{LocalDateTime, ZoneOffset}

import org.bruchez.olivier.msatozip.tree._

object Zip {
  def saveToZip(directory: Directory, zipFile: Path): Unit = withZipFileSystem(zipFile) { fs =>
    def write(currentDirectory: Directory, currentPath: Path): Unit = {
      for (child <- currentDirectory.children) {
        child match {
          case d: Directory =>
            val directoryPath = Paths.get("") // @todo

            write(d, directoryPath)
          case f: File =>
            val filePath = Paths.get("") // @todo

            // val pathInZipfile = fs.getPath("/SomeTextFile.txt")

            Files.write(
              filePath,
              f.data.toArray,
              StandardOpenOption.WRITE,
              StandardOpenOption.CREATE,
              StandardOpenOption.TRUNCATE_EXISTING
            )

            f.dateTime.foreach(dateTime =>
              Files.setLastModifiedTime(filePath, fileTimeFromLocalDateTime(dateTime))
            )

          // @todo set modification date
          case _: CorruptedFile =>
        }
      }

    }

    // @todo can empty directory be created?
    // @todo can last modification date be added to directories?

    write(directory, Paths.get(""))
  }

  protected def fileTimeFromLocalDateTime(dateTime: LocalDateTime): FileTime =
    FileTime.fromMillis(dateTime.toInstant(ZoneOffset.UTC).toEpochMilli)

  protected def withZipFileSystem[T](zipFile: Path)(f: FileSystem => T): T = {
    val env = new java.util.HashMap[String, String]()
    env.put("create", "true")

    val uri = URI.create("jar:" + zipFile.toString)

    val zipFileSystem = FileSystems.newFileSystem(uri, env)

    try {
      f(zipFileSystem)
    } finally {
      Option(zipFileSystem).foreach(_.close())
    }
  }
}
