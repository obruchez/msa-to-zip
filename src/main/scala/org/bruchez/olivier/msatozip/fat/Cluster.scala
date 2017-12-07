package org.bruchez.olivier.msatozip.fat

sealed trait Cluster

case object AvailableCluster extends Cluster

case class NextEntry(cluster: Int) extends Cluster

case class Reserved(value: Int) extends Cluster

case object BadSector extends Cluster

case class LastCluster(value: Int) extends Cluster

object Cluster {
  def apply(cluster: Int, value: Int): Cluster = {
    if (value == 0x000) {
      AvailableCluster
    } else if (value >= 0x002 && value <= 0xfef) {
      NextEntry(value)
    } else if (value >= 0xff0 && value <= 0xff6) {
      Reserved(value)
    } else if (value == 0xff7) {
      BadSector
    } else if (value >= 0xff8 && value <= 0xfff) {
      LastCluster(value)
    } else {
      throw new UnexpectedClusterValueException(cluster, value)
    }
  }
}

abstract class ClusterException(val corruption: Corruption)
    extends Exception(corruption.description)

class UnexpectedClusterValueException(cluster: Int, value: Int)
    extends ClusterException(UnexpectedClusterValue(cluster, value))

class EmptyClusterException(cluster: Int) extends ClusterException(EmptyCluster(cluster))

class ReservedClusterException(cluster: Int) extends ClusterException(ReservedCluster(cluster))

class BadClusterException(cluster: Int) extends ClusterException(BadCluster(cluster))
