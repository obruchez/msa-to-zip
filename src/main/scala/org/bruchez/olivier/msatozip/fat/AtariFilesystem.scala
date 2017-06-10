package org.bruchez.olivier.msatozip.fat

import java.io.{ ByteArrayInputStream, DataInputStream }

import org.bruchez.olivier.msatozip.msa.MsaImage

class AtariFilesystem(msaImage: MsaImage) {
  private def withData[T](offset: Int)(f: DataInputStream => T): T = {
    val is = new DataInputStream(new ByteArrayInputStream(msaImage.data.drop(offset)))

    try {
      f(is)
    } finally {
      is.close()
    }
  }

  lazy val bootSector: BootSector = withData(offset = 0)(BootSector(_))
}
