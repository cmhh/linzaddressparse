package org.cmhh.linzaddressparse

import scala.io.Source
import scala.util.Random

/**
 * Address type aliases / abbreviations.
 */
object StreetTypeAlias {
  private val r = new scala.util.Random()
  
  /**
   * Get all aliases for a street type.
   *
   * @param x street type
   * @return vector holding each alias
   */
  def apply(x: String): Vector[String] = {
    m.getOrElse(x.toLowerCase(), Vector.empty)
  }

  /**
   * Get a random alias.
   *
   * @param x street type
   * @param includeSelf whether x can be considered an alias of itself
   * @return a street type alias
   */
  def random(x: String, includeSelf: Boolean): String = {
    if (!m.contains(x.toLowerCase())) x
    else {
      val choices = 
        if (includeSelf) m(x.toLowerCase()) 
        else m(x.toLowerCase()).filter(y => y.toLowerCase != x.toLowerCase)

      if (choices.size == 0) x
      else choices(r.nextInt(choices.size))
    }
  }

  /**
   * Get a random alias.
   *
   * @param x street type
   * @param includeSelf whether x can be considered an alias of itself
   * @return a street type alias
   */
  def random(x: Option[String], includeSelf: Boolean): Option[String] = {
    x.map(y => random(y, includeSelf))
  }

  private lazy val m: Map[String, Vector[String]] = {
    val lines = Source.fromResource("street_abbr.txt").getLines()

    def loop(it: Iterator[String], accum: Map[String, Vector[String]]): Map[String, Vector[String]] = {
      if (!it.hasNext) accum 
      else {
        val line = it.next()
        val parts = line.split('|').toVector
        loop(it, accum + (parts.head.toLowerCase() -> parts))
      }
    }

    loop(lines, Map.empty)
  }
}