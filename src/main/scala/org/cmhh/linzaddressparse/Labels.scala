package org.cmhh.linzaddressparse

/**
 * Functions for working with class labels.
 *
 * Probably things in the dl4j library that can do this... but I haven't read the docs thoroughly.
 */
object Labels {
  /**
   * One-hot representation of single label.
   *
   * @param x text represenation of label
   * @return a binary array.
   */
  def apply(x: String): Vector[Int] = onehot.getOrElse(x.toLowerCase, utils.zeros(names.size))

  /**
   * Complete set of labels.
   */
  val names: Vector[String] = Vector(
    "space", "separator", 
    "unit_type", "unit_value", "level_type", "level_value", 
    "address_number", "address_number_suffix", "address_number_high", 
    "road_name", "road_type_name", "road_suffix", 
    "suburb_locality", "postcode", "town_city"
  )

  val size = names.size

  /**
   * Map holding all one-hot represenations.
   */
  val onehot: Map[String, Vector[Int]] = {
    def loop(d: Vector[String], accum: Map[String, Vector[Int]]): Map[String, Vector[Int]] = {
      if (d.size == 1) {
        accum + (d.head -> utils.onehot(d.head, names))
      } else {
        loop(d.tail,  accum + (d.head -> utils.onehot(d.head, names)))
      }
    }

    loop(names, Map.empty)
  }
}