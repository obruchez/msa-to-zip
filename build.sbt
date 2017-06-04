name := "msa-to-zip"
version := "1.0"
scalaVersion := "2.12.2"

libraryDependencies += "de.waldheinz" % "fat32-lib" % "0.6.5"

mainClass in assembly := Some("org.bruchez.olivier.msatozip.MsaToZip")

assemblyJarName in assembly := "msa-to-zip.jar"
