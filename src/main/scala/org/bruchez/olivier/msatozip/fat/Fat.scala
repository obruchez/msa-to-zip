package org.bruchez.olivier.msatozip.fat

import org.bruchez.olivier.msatozip.DataInputStreamHelper._

import java.io.DataInputStream

import scala.collection.mutable.ListBuffer

case class Fat(clusters: Seq[Cluster]) {
  def clustersFromStartingCluster(startingCluster: Int): Seq[Int] = {
    @annotation.tailrec
    def clustersFromStartingCluster(clusterIndex: Int, reverseAcc: List[Int]): Seq[Int] =
      clusters(clusterIndex) match {
        case LastCluster(_) =>
          (clusterIndex +: reverseAcc).reverse
        case AvailableCluster =>
          throw new EmptyClusterException(clusterIndex)
        case Reserved(_) =>
          throw new ReservedClusterException(clusterIndex)
        case BadSector =>
          throw new BadClusterException(clusterIndex)
        case NextEntry(nextClusterIndex) =>
          clustersFromStartingCluster(nextClusterIndex, clusterIndex +: reverseAcc)
      }

    clustersFromStartingCluster(startingCluster, reverseAcc = List())
  }
}

object Fat {
  def apply(is: DataInputStream, clusterCount: Int): Fat = {
    assert(clusterCount % 2 == 0, s"Unexpected cluster count: $clusterCount")

    val clusters = ListBuffer[Cluster]()

    for (clusterIndex <- 0 until clusterCount by 2) {
      val clusterPair = is.readUnsigned24LittleEndian()

      clusters.append(Cluster(cluster = clusterIndex, value = (clusterPair >> 0) & 0xfff))
      clusters.append(Cluster(cluster = clusterIndex + 1, value = (clusterPair >> 12) & 0xfff))
    }

    Fat(clusters)
  }
}
