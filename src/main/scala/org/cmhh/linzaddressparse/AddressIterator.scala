package org.cmhh.linzaddressparse

import java.io.{InputStream, FileInputStream}
import java.util.zip.GZIPInputStream
import scala.io.Source
import scala.util.{Try, Success, Failure}

/**
 * Address iterator.
 */
case class AddressIterator(
  val is: InputStream, val gz: Boolean
) extends Iterator[Try[AddressComponents]] {
  private val it = gz match {
    case true => Source.fromInputStream(new GZIPInputStream(is)).getLines()
    case false => Source.fromInputStream(is).getLines()
  }

  def hasNext: Boolean = it.hasNext

  def next(): Try[AddressComponents] = Try {
    val parts = it.next().split('|')

    val args: List[Option[String]] = parts
      .map(x => if (x.trim() == "") None else Some(x.trim()))
      .toList

    AddressComponents(
      args(0), args(1), args(2), args(3), args(4), args(5), args(6), args(7),
      args(8), args(9), args(10), args(11), 
      if (args.size == 13) args(12) else None
    )
  }

  def collect(): Vector[AddressComponents] = {
    def loop(accum: Vector[AddressComponents]): Vector[AddressComponents] = {
      if (hasNext) {
        next() match {
          case Success(x) => loop(accum :+ x)
          case Failure(x) => loop(accum)
        }
      } else accum
    }

    loop(Vector.empty)
  }

  def collect(n: Int): Vector[AddressComponents] = {
    def loop(i: Int, accum: Vector[AddressComponents]): Vector[AddressComponents] = {
      if (i <= 0) accum
      else if (hasNext) {
        next() match {
          case Success(x) => loop(i - 1, accum :+ x)
          case Failure(x) => loop(i, accum)
        }
      } else accum
    }

    loop(n, Vector.empty)
  }
}

case object AddressIterator {
  def apply(filename: String, gz: Boolean): AddressIterator = 
    AddressIterator(new FileInputStream(filename), gz)

  /**
   * Training set (80% of full LINZ address set).
   */
  def train = AddressIterator(getClass.getResourceAsStream("/linzaddress_train.csv.gz"), true)

  /**
   * Training set (20% of full LINZ address set).
   */
  def test = AddressIterator(getClass.getResourceAsStream("/linzaddress_test.csv.gz"), true)
}