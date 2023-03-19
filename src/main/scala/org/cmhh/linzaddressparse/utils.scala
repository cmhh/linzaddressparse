package org.cmhh.linzaddressparse

import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import org.deeplearning4j.nn.graph.ComputationGraph
import org.nd4j.linalg.dataset.DataSet

import java.io._

/**
 * A random collection of useful functions.
 *
 * Maybe these should be organised into different objects...
 */
object utils {
  private val r = new scala.util.Random()

  /**
   * One-hot encoding for a label, given a set of labels.
   *
   * @param x single label
   * @param labels complete set of labels
   * @return one-hot encoding
   */
  def onehot[T](x: T, labels: Vector[T]): Vector[Int] = {
    val pos = labels.indexOf(x)

    if (pos < 0) {
      zeros(labels.size)
    } else {
      zeros(pos) ++ Vector(1) ++ zeros(labels.size - pos - 1)
    }
  }

  /**
   * Create a vector of zeroes.
   *
   * @param n size of vector
   * @return a vector containing {{n}} zeroes.
   */
  def zeros(n: Int): Vector[Int] = 
    if (n <= 0) {
      Vector.empty
    } else {
      {1 to n}.map(x => 0).toVector
    }
  
  /**
   * Create a vectory by repeating `x` `n` times.
   *
   * @param x character to repeat
   * @param n number of times to repeat x
   * @return vector of `n` `x`s
   */
  def rep[T](x: T, n: Int): Vector[T] = 
    (1 to n).toVector.map(i => x)

  /**
   * Create a feature / label mask.
   * 
   * @param length size of vector
   * @param index selected element
   * @return vector of `0`s, and a single `1` at `index`
   */
  def select(length: Int, index: Int): Vector[Int] = {
    (1 to length).toVector.map(i => if (i == index) 1 else 0)
  }

  /**
   * Create a feature / label mask.
   * 
   * @param length size of vector
   * @param n `n` `1`s followed by `length -n` `0`s
   */
  def selectTo(length: Int, n: Int): Vector[Int] = {
    (1 to length).toVector.map(i => if (i <= n) 1 else 0)
  }

  /**
   * Pad a labelled set of characters with `space`.
   *
   * @param x a tagged set of characters
   * @param n the length the sequence should be
   * @return the original sequence with enough `space`s to make it `n` in length.
   */
  def padLabelledChars(x: Tagged, n: Option[Int]): Tagged = {
    n match {
      case None => x
      case Some(n_) => 
        val m = x._1.size
        x ++ Tagged(utils.rep(' ', n_ - m), utils.rep("space", n_ - m))
    }
  }

  /**
   * One-hot encoding of tagged sets of characters.
   *
   * @param x tagged set of characters
   * @return tuple with one-hot encoded characters and labels
   */
  def features(x: Tagged): (Vector[Vector[Int]], Vector[Vector[Int]]) = {
    (x._1.map(y => Vocab(y)), x._2.map(y => Labels(y)))
  }

  /**
   * Convert vectors to {{org.nd4j.linalg.api.ndarray.INDArray}}.
   *
   * @param x one-hot encoded vectors
   * @return tuple of {{org.nd4j.linalg.api.ndarray.INDArray}}
   */
  def toNd4j(x: (Vector[Vector[Int]], Vector[Vector[Int]])): (INDArray, INDArray) = {
    val features = Nd4j.createFromArray(x._1.map(a => a.toArray).toArray).transpose()
    val labels = Nd4j.createFromArray(x._2.map(a => a.toArray).toArray).transpose()
    (features, labels)
  }

  /**
   * Create mask.
   *
   * @param length size of vector
   * @param index selected element
   * @return {{org.nd4j.linagl.api.ndarray.INDArray}} of `0`s, and a single `1` at `index`
   */
  def mask(m: Int, n: Int): INDArray = {
    Nd4j.createFromArray(Array((1 to m).map(i => if (i <= n) 1 else 0).toArray))
  }

  /**
   * One-hot encoding of complete address string.
   *
   * @param x address string
   * @return {{org.nd4j.linagl.api.ndarray.INDArray}}
   */
  def encode(x: String): INDArray = {
    val onehot = x.toLowerCase.toArray.map(x => Vocab(x).toArray)
    Nd4j.pile(Nd4j.createFromArray(onehot).transpose)
  }

  /**
   * Convert a DataSet to a set of tagged characters.
   *
   * @param x DataSet
   * @param i which record in DataSet to decode
   * @return tagged sequence of characters.
   */
  def decodeDataSet(x: DataSet, i: Int): Tagged = {
    val f = decodeFeatures(x.getFeatures(), i)
    val l = decodeLabels(x.getLabels(), i)
    val m = x.getFeaturesMaskArray().slice(i).toFloatVector.toVector

    Tagged(
      f.zip(m).filter(_._2 > 0).map(_._1),
      l.zip(m).filter(_._2 > 0).map(_._1)
    )
  }

  /**
   * Convert array of features to sequence of characters.
   *
   * @param x {{org.nd4j.linagl.api.ndarray.INDArray}}
   * @return sequence of characters.
   */
  def decodeFeatures(x: INDArray): Vector[Char] = {
    val n = x.shape()(1).toInt
    val x_ = x.transpose()

    (0 until n).toVector.map(j => {
      val y = x_.slice(j).toFloatVector()
      Vocab.names(y.indexOf(1))
    })
  }

  /**
   * Convert array of labels to sequence of strings.
   *
   * @param x {{org.nd4j.linagl.api.ndarray.INDArray}}
   * @return sequence of strings.
   */
  def decodeLabels(x: INDArray): Vector[String] = {
    val n = x.shape()(1).toInt
    val x_ = x.transpose()

    (0 until n).toVector.map(j => {
      val y = x_.slice(j).toFloatVector()
      Labels.names(y.indexOf(y.max))
    })
  }

  /**
   * Convert array of features to sequence of characters.
   *
   * @param x {{org.nd4j.linagl.api.ndarray.INDArray}}
   * @param i index of record to decode
   * @return sequence of characters.
   */
  def decodeFeatures(x: INDArray, i: Int): Vector[Char] = {
    val n = x.shape()(1).toInt
    val x_ = x.slice(i).transpose()

    (0 until n).toVector.map(j => {
      val y = x_.slice(j).toFloatVector()
      Vocab.names(y.indexOf(1))
    })
  }

  /**
   * Convert array of labels to sequence of strings.
   *
   * @param x {{org.nd4j.linagl.api.ndarray.INDArray}}
   * @param i index of record to decode
   * @return sequence of strings.
   */
  def decodeLabels(x: INDArray, i: Int): Vector[String] = {
    val n = x.shape()(2).toInt
    val x_ = x.slice(i).transpose()

    (0 until n).toVector.map(j => {
      val y = x_.slice(j).toFloatVector()
      Labels.names(y.indexOf(y.max))
    })
  }

  /**
   * Parse and address string into components.
   *
   * @param x address string
   * @param m address parser model
   * @return parsed address components
   */
  def parse(x: String)(implicit m: ComputationGraph): AddressComponents = {
    val yhat = m.output(encode(x))
    val labels = decodeLabels(yhat(0), 0)
    val tagged = x.toVector.zip(labels)

    def get(label: String): Option[String] = {
      val res = tagged.filter(_._2 == label).map(_._1).mkString
      if (res.size == 0) None else Some(res)
    }

    AddressComponents(
      get("unit_type"), get("unit_value"), 
      get("level_type"), get("level_value"), 
      get("address_number"), get("address_number_suffix"), get("address_number_high"),
      get ("road_name"), get("road_type_name"), get("road_suffix"), 
      get("suburb_locality"), get("postcode"), get("town_city")
    )
  }

  /**
   * Output an address string with label distribution for each character.
   *
   * @param x address string
   * @param m address parser model
   * @return characters with probability of each label.
   */
  def parseWithScores(x: String)(implicit m: ComputationGraph): Vector[(Char, String, Double)] = {
    val yhat = m.output(encode(x))
    val labels = decodeLabels(yhat(0), 0)
    val scores = yhat(0).slice(0).transpose.toDoubleMatrix

    (0 until x.size).flatMap(i => {
      (0 until Labels.size).map(j => {
        (x(i), Labels.names(j), scores(i)(j))
      }).toVector
    }).toVector
  }

  /**
   * Save an address string to file with label distribution for each character.
   *
   * @param x address string
   * @param f output file name
   * @param m address parser model
   */
  def parseWithScores(x: String, f: String)(implicit m: ComputationGraph): Unit = {
    val xs = parseWithScores(x)(m)
    
    val file = new File(f)
    val bw = new BufferedWriter(new FileWriter(file))

    bw.write("char,label,score")

    xs.foreach(x => {
      bw.write(s"""\n"${x._1}","${x._2}",${x._3}""")
    })
    
    bw.close()
  }

  /**
   * Introduce typos.
   *
   * @param x character
   * @param p probability of typo
   */
  def typo(x: Char, p: Double): Char = {
    val mapping: Map[Char, Vector[Char]] = Map(
      'a' -> Vector('a', 'q', 'w', 's'),
      'b' -> Vector('b', 'v', 'g', 'h', 'n'),
      'c' -> Vector('c', 'x', 'd', 'f', 'v'),
      'd' -> Vector('d', 's', 'e', 'f', 'c', 'x'),
      'e' -> Vector('e', 'w', 'd', 'r'),
      'f' -> Vector('f', 'd', 'r', 'g', 'v', 'c'),
      'g' -> Vector('g', 'f', 't', 'h', 'b', 'v'),
      'h' -> Vector('h', 'g', 'y', 'j', 'n', 'b'),
      'i' -> Vector('i', 'u', 'k', 'o'),
      'j' -> Vector('j', 'h', 'u', 'k', 'm', 'n'),
      'k' -> Vector('k', 'j', 'i', 'l', 'm'),
      'l' -> Vector('l', 'k', 'o'),
      'm' -> Vector('m', 'n', 'j', 'k', 'l'),
      'n' -> Vector('n', 'b', 'h', 'j', 'm'),
      'o' -> Vector('o', 'i', 'l', 'p'),
      'p' -> Vector('p', 'o', 'l'),
      'q' -> Vector('q', 'a', 's', 'w'),
      'r' -> Vector('r', 'e', 'f', 't'),
      's' -> Vector('s', 'a', 'w', 'd', 'x', 'z'),
      't' -> Vector('t', 'r', 'g', 'y'),
      'u' -> Vector('u', 'y', 'j', 'i'),
      'v' -> Vector('v', 'c', 'f', 'g', 'b'),
      'w' -> Vector('w', 'q', 's', 'e'),
      'x' -> Vector('x', 'z', 's', 'd', 'c'),
      'y' -> Vector('y', 't', 'h', 'u'),
      'z' -> Vector('z', 'a', 's', 'x')
    )

    if (mapping.contains(x) & r.nextDouble() < p) {
      mapping(x)(r.nextInt(mapping(x).size))
    } else x
  }

  /**
   * Introduce typos.
   *
   * @param x string
   * @param p probability of typo
   */
  def typo(x: String, p: Double): String = {
    x.map(typo(_, p))
  }

  /**
   * Introduce typos.
   *
   * @param x string
   * @param p probability of typo
   */
  def typo(x: =>Option[String], p: Double): Option[String] = {
    x.map(typo(_, p))
  }
}