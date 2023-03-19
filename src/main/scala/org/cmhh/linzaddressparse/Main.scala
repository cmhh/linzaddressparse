package org.cmhh.linzaddressparse

import org.deeplearning4j.util.ModelSerializer
import org.deeplearning4j.nn.graph.ComputationGraph
import scala.util.{Try, Success, Failure}

/**
 * Parse a single address using pre-trained model.
 */
object AddressParse extends App {
  implicit val m: ComputationGraph = 
    ModelSerializer.restoreComputationGraph(getClass.getResourceAsStream("/model.mdl"))

  println(utils.parse(args(0)).toJson)
}