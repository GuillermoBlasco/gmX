import sbt._

resolvers += Resolver.mavenLocal

name := "gmX"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies += "org.apache.spark" % "spark-core_2.10" % "1.2.0-SNAPSHOT"

libraryDependencies += "org.apache.spark" % "spark-graphx_2.10" % "1.2.0-SNAPSHOT"
