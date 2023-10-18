package org.cmhh.linzaddressparse

import org.nd4j.linalg.dataset.api.iterator.DataSetIterator
import org.nd4j.linalg.dataset.DataSet
import org.nd4j.linalg.api.ndarray.INDArray
import org.nd4j.linalg.factory.Nd4j
import scala.jdk.CollectionConverters._
import scala.util.{Try, Success, Failure}

/**
 * DataSet iterator which can be used to train / evaluate LSTM model.
 */
class AddressDataSetIterator(
  it: AddressIterator,
  batchSize: Int, 
  pDropUnit: Double = 0.1, 
  pSwapRoadType: Double = 0.5, 
  pDropSuburb: Double = 0.1, 
  pDropTownCity: Double = 0.1,
  pDropPostcode: Double = 0.1, 
  pTypo: Double = 0.02,
  pNoCommas: Double = 0.1,
) extends DataSetIterator {
  val asyncSupported: Boolean = false
  val batch: Int = batchSize
  val getLabels: java.util.List[String] = Labels.names.asJava
  val getPreProcessor: org.nd4j.linalg.dataset.api.DataSetPreProcessor = null 
  val inputColumns: Int = Vocab.size
  val totalOutcomes: Int = Labels.size
  val resetSupported: Boolean = false
  def hasNext(): Boolean = it.hasNext
  def setPreProcessor(p: org.nd4j.linalg.dataset.api.DataSetPreProcessor): Unit = ()
  def reset(): Unit = ()

  def next(): DataSet = next(batch)

  def next(n: Int): DataSet =  {
    def loop(n: Int, accum: Seq[AddressComponents]): Seq[AddressComponents] = {
      if (!it.hasNext || n < 1) accum
      else it.next() match {
        case Success(addr) => 
          loop(
            n - 1, 
            accum :+ addr.perturb(pDropUnit, pSwapRoadType, pDropSuburb, pDropTownCity, pDropPostcode, pTypo)
          )
        case Failure(e) => loop(n, accum)
      }
    }

    val addresses = 
      loop(n, Vector.empty)
        .map(x => x.labelledChars(pDropUnit, pNoCommas))
        
    val maxLength = addresses.map(x => x._1.size).max
    
    val paddedFeatures = addresses.map(x => {
      utils.toNd4j(utils.features(utils.padLabelledChars(x, Some(maxLength))))
    })

    val features = Nd4j.pile(paddedFeatures.map(_._1): _*)
    val labels = Nd4j.pile(paddedFeatures.map(_._2): _*)    
    val mask = Nd4j.vstack(addresses.map(x => utils.mask(maxLength, x.size)).toArray: _*)

    new DataSet(features, labels, mask, mask)
  }
}

object AddressDataSetIterator {
  /**
   * Training set (80% of full LINZ address set).
   */
  def train = new AddressDataSetIterator(
    AddressIterator(getClass.getResourceAsStream("/linzaddress_train.csv.gz"), true),
    32
  )

  /**
   * Training set (20% of full LINZ address set).
   */
  def test = new AddressDataSetIterator(
    AddressIterator(getClass.getResourceAsStream("/linzaddress_test.csv.gz"), true),
    32
  )
}