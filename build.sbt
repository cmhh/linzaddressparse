import Dependencies._
import sbt._

ThisBuild / scalaVersion     := "2.13.10"
ThisBuild / version          := "0.0.1"
ThisBuild / organization     := "org.cmhh"
ThisBuild / organizationName := "cmhh"

scalacOptions ++= Seq("-deprecation", "-feature")

lazy val root = (project in file("."))
  .settings(
    name := "linzaddressparse",
    libraryDependencies ++= Seq(
      dl4jcore, nd4j, 
      pg, 
      slick,
      hikaricp,
      conf,
      slf4j,
      scalatest % Test
    ), 
    
    ThisBuild / assemblyMergeStrategy := {
      case PathList("reference.conf") => MergeStrategy.concat
      case "reference.conf" => MergeStrategy.concat
      case "application.conf" => MergeStrategy.concat
      case n if n.contains("services") => MergeStrategy.concat
      case PathList("META-INF", xs @ _*) => MergeStrategy.discard
      case x => MergeStrategy.first
    },

    assembly / assemblyJarName := "linzaddressparse.jar"
  )