package org.bruchez.olivier.msatozip.fat

import org.bruchez.olivier.msatozip.DataInputStreamHelper._

import java.io.DataInputStream

import scala.collection.mutable.ListBuffer
import scala.util.Try

case class Fat(clusters: Seq[Cluster]) {
  def clustersFromStartingCluster(startingCluster: Int): Seq[Int] = {
    @annotation.tailrec
    def clustersFromStartingCluster(clusterIndex: Int, reverseAcc: List[Int]): Seq[Int] = {
      val cluster = clusters(clusterIndex)

      cluster match {
        case LastCluster(_) =>
          (clusterIndex +: reverseAcc).reverse
        case AvailableCluster | Reserved(_) | BadSector =>
          throw new Exception(s"File contains empty, reserved, or bad cluster")
        case NextEntry(nextClusterIndex) =>
          clustersFromStartingCluster(nextClusterIndex, clusterIndex +: reverseAcc)
      }
    }

    clustersFromStartingCluster(startingCluster, reverseAcc = List())
  }
}

object Fat {
  def apply(is: DataInputStream, clusterCount: Int): Fat = {
    assert(clusterCount % 2 == 0, s"Unexpected cluster count: $clusterCount")

    val clusters = ListBuffer[Cluster]()

    for (i <- 1 to clusterCount / 2) {
      val clusterPair = is.readUnsigned24LittleEndian()

      clusters.append(Cluster(value = (clusterPair >> 0) & 0xfff))
      clusters.append(Cluster(value = (clusterPair >> 12) & 0xfff))
    }

    Fat(clusters)
  }
}
