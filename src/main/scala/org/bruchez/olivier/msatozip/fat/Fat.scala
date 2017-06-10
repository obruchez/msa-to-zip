package org.bruchez.olivier.msatozip.fat

import org.bruchez.olivier.msatozip.DataInputStreamHelper._

import java.io.DataInputStream

import scala.collection.mutable.ListBuffer

case class Fat(clusters: Seq[Cluster])

object Fat {
  def apply(is: DataInputStream, clusterCount: Int): Fat = {
    assert(clusterCount % 2 == 0, s"Unexpected cluster count: $clusterCount")

    val clusters = ListBuffer[Cluster]()

    for (i <- 1 to clusterCount) {
      val clusterPair = is.readUnsigned24LittleEndian()

      clusters.append(Cluster(value = (clusterPair >> 0) & 0xfff))
      clusters.append(Cluster(value = (clusterPair >> 12) & 0xfff))
    }

    Fat(clusters)
  }
}
