package org.cmhh.linzaddressparse

/**
 * Functions for working with features / vocabulary.
 *
 * Probably things in the dl4j library that can do this... but I haven't read the docs thoroughly.
 */
object Vocab {
  /**
   * One-hot representation of single label.
   *
   * @param x text represenation of label
   * @return a binary array.
   */
  def apply(x: Char): Vector[Int] = onehot.getOrElse(x.toLower, utils.zeros(names.size))

  /**
   * Complete vocabulary.
   */
  val names: Vector[Char] = 
    Vector(' ', '\'', ',', '-', '/') ++ 
      {'0' to '9'}.toVector ++ {'a' to 'z'}.toVector ++ 
      Vector(257.toChar, 275.toChar, 299.toChar, 333.toChar, 363.toChar)

  val size = names.size
  
  /**
   * Map holding all one-hot represenations.
   */
  val onehot: Map[Char, Vector[Int]] = {
    def loop(d: Vector[Char], accum: Map[Char, Vector[Int]]): Map[Char, Vector[Int]] = {
      if (d.size == 1) {
        accum + (d.head -> utils.onehot(d.head, names))
      } else {
        loop(d.tail,  accum + (d.head -> utils.onehot(d.head, names)))
      }
    }

    loop(names, Map.empty)
  }
}