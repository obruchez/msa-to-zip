package org.bruchez.olivier.msatozip

import java.nio.ByteBuffer

class MsaBlockDevice(msaImage: MsaImage) extends de.waldheinz.fs.BlockDevice {
  private var closed = false

  override def close(): Unit = {
    closed = true
  }

  override def flush(): Unit = ()

  override def getSectorSize: Int = msaImage.sectorLength

  override def getSize: Long = msaImage.totalLength

  override def isClosed: Boolean = closed

  override def isReadOnly: Boolean = true

  override def read(devOffset: Long, dest: ByteBuffer): Unit = {
    //println(s"READ, offset = $devOffset, remaining = ${dest.remaining}")
    dest.put(msaImage.data.slice(devOffset.toInt, devOffset.toInt + dest.remaining))
  }

  override def write(devOffset: Long, src: ByteBuffer): Unit = ()
}
