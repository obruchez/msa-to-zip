package org.bruchez.olivier.msatozip.fat

sealed trait Corruption { def description: String }

case class UnexpectedClusterValue(cluster: Int, value: Int) extends Corruption {
  override val description = s"Unexpected cluster value $value at cluster $cluster"
}

case class EmptyCluster(cluster: Int) extends Corruption {
  override val description = s"Empty cluster $cluster"
}

case class ReservedCluster(cluster: Int) extends Corruption {
  override val description = s"Reserved cluster $cluster"
}

case class BadCluster(cluster: Int) extends Corruption {
  override val description = s"Reserved cluster $cluster"
}
