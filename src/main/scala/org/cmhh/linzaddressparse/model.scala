package org.cmhh.linzaddressparse

import org.deeplearning4j.nn.conf.NeuralNetConfiguration
import org.deeplearning4j.nn.conf.graph.MergeVertex
import org.deeplearning4j.nn.conf.layers.{LSTM, RnnOutputLayer}
import org.deeplearning4j.nn.conf.layers.recurrent.{Bidirectional}
import org.deeplearning4j.nn.graph.ComputationGraph
import org.deeplearning4j.nn.api.OptimizationAlgorithm
import org.nd4j.linalg.activations.Activation
import org.deeplearning4j.nn.weights.WeightInit
import org.nd4j.linalg.learning.config.{Nesterovs, Sgd, Adam}
import org.nd4j.linalg.lossfunctions.LossFunctions 
import org.nd4j.linalg.dataset.DataSet
import org.deeplearning4j.nn.conf.inputs.InputType
import org.nd4j.linalg.factory.Nd4j
import java.io.File

/**
 * Model-related functions.
 */
object model {
  /**
   * Create address arser model.
   *
   * @param vocabSize vocabulary size
   * @param numClasses number of classes / labels
   * @param numHiddenNodes1 number of hidden nodes in first LSTM layer
   * @param numHiddenNodes2 number of hidden nodes in second LSTM layer
   * @param learningRate learning rate
   * @param seed random seed
   * 
   * @return [[org.deeplearning4j.nn.graph.ComputationGraph]]
   */
  def lstm(
    vocabSize: Int, numClasses: Int, 
    numHiddenNodes1: Int = 80, numHiddenNodes2: Int = 200, 
    learningRate: Double = 0.005, seed: Int = 1234
  ): ComputationGraph = {
    val architecture = new NeuralNetConfiguration.Builder()
      .weightInit(WeightInit.XAVIER)
      .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
      .updater(new Adam(learningRate))
      .seed(seed)
      .graphBuilder()
      .addInputs("features")
      .setOutputs("prediction")
      .setInputTypes(InputType.recurrent(vocabSize))
      .addLayer(
        "bdlstm1", 
        new Bidirectional(
          new LSTM.Builder()
            .nIn(vocabSize)
            .nOut(numHiddenNodes1)
            .activation(Activation.TANH)
            .build()
        ), 
        "features"
      )
      .addLayer(
        "bdlstm2", 
        new Bidirectional(
          new LSTM.Builder()
            .nIn(numHiddenNodes1)
            .nOut(numHiddenNodes2)
            .activation(Activation.TANH)
            .build()
        ), 
        "bdlstm1"
      )
      .addLayer(
        "prediction", 
        new RnnOutputLayer.Builder()
          .nIn(numHiddenNodes2)
          .nOut(numClasses)
          .activation(Activation.SOFTMAX)
          .lossFunction(LossFunctions.LossFunction.MCXENT)
          .build(), 
        "bdlstm2")
      .build()

    val network = new ComputationGraph(architecture)
    network.init()
    network
  }

  /**
   * Save model to disk.
   * 
   * @param m address parser model
   * @param file file name 
   */
  def save(m: ComputationGraph, file: String): Unit = {
    m.save(new File(file))
  }

  /**
   * Load model from disk.
   */
  def load(file: String): ComputationGraph = {
    ComputationGraph.load(new File(file), true) 
  }

  /** 
   * Calculate parser accuracy for single batch.
   *
   * An address is parsed correctly if _every_ character is correctly labelled.
   *
   * @param batch DataSet
   * @param m address parser model
   * @return tuple with number of correctly parsed addresses and total number of addresses
   */
  def accuracy(batch: DataSet)(implicit m: ComputationGraph): (Int, Int) = {
    val features = batch.getFeatures()
    val labels = batch.getLabels()
    val mask = batch.getFeaturesMaskArray()

    val fit = m.output(batch.getFeatures())(0)
    
    val matches = (0 until (fit.shape())(0).toInt).map(i => {
      val m = mask.slice(i).toDoubleVector
      val yhat = utils.decodeLabels(fit, i).zip(m).filter(_._2 == 1.0).map(_._1)
      val y = utils.decodeLabels(labels, i).zip(m).filter(_._2 == 1.0).map(_._1)
      yhat.zip(y).map(x => if (x._1 == x._2) 1 else 0).sum == m.sum
    })

    (matches.filter(x => x).size, matches.size)
  }

  /** 
   * Calculate parser accuracy.
   *
   * An address is parsed correctly if _every_ character is correctly labelled.
   *
   * @param it DataSet iterator
   * @param m address parser model
   * @return accuracy rate
   */
  def accuracy(it: AddressDataSetIterator)(implicit m: ComputationGraph): Double = {
    def loop(it: AddressDataSetIterator, y: Int, n: Int): Double = {
      if (!it.hasNext()) y.toDouble / n.toDouble else {
        val res = accuracy(it.next())(m)
        loop(it, y + res._1, y + res._2)
      }
    }

    loop(it, 0, 0)
  }

  /** 
   * Calculate parser accuracy.
   *
   * An address is parsed correctly if _every_ character is correctly labelled.
   *
   * @param it DataSet iterator
   * @param m address parser model
   * @param nbatches number of batches
   * @return accuracy rate
   */
  def accuracy(it: AddressDataSetIterator, nbatches: Int)(implicit m: ComputationGraph): Double = {
    def loop(it: AddressDataSetIterator, y: Int, n: Int, b: Int): Double = {
      if (b < 0) {
        y.toDouble / n.toDouble
       } else if (!it.hasNext()) {
        y.toDouble / n.toDouble
       } else {
        val res = accuracy(it.next())(m)

        loop(it, y + res._1, y + res._2, b - 1)
      }
    }

    loop(it, 0, 0, nbatches)
  } 
}