package org.cmhh.linzaddressparse

/**
 * Simple class to represent a set of tagged characters.
 */
case class Tagged(features: Vector[Char], labels: Vector[String]) {
  val size: Int = features.size
  
  /**
   * Concatenate tagged characters.
   *
   * @param x set to append
   * @param a tagged set
   */
  def `++`(x: Tagged): Tagged = {
    Tagged(features ++ x.features, labels ++ x.labels)
  }

  /**
   * Behave a bit like a tuple
   */
  val _1: Vector[Char] = features
  val _2: Vector[String] = labels
}

object Tagged {
  def apply(x: Char, y: String): Tagged = Tagged(Vector(x), Vector(y))
  def empty: Tagged = Tagged(Vector.empty, Vector.empty)
}