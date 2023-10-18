import sbt._

object Dependencies {
  /**/
  lazy val dl4jcore = "org.deeplearning4j" % "deeplearning4j-core" % "1.0.0-M1.1"
  lazy val nd4j = "org.nd4j" % "nd4j-native-platform" % "1.0.0-M1.1"
  /**/
  /*
  lazy val dl4jcore = "org.deeplearning4j" % "deeplearning4j-cuda-11.2" % "1.0.0-M1.1"
  lazy val nd4j = "org.nd4j" % "nd4j-cuda-11.2-platform" % "1.0.0-M1.1"
  */
  /*
  lazy val pg = "org.postgresql" % "postgresql" % "42.5.3"
  lazy val slick = "com.typesafe.slick" %% "slick" % "3.4.1"
  lazy val hikaricp = "com.typesafe.slick" %% "slick-hikaricp" % "3.4.1"
  */
  lazy val conf = "com.typesafe" % "config" % "1.4.2"
  lazy val slf4j = "org.slf4j" % "slf4j-nop" % "2.0.6" 
  lazy val scalatest = "org.scalatest" % "scalatest_2.13" % "3.2.15" 
}