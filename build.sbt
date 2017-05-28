name := """micro-chat"""
organization := "com.micro"

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.11"

libraryDependencies += filters
libraryDependencies ++= Seq(
  "org.scalatestplus.play" %% "scalatestplus-play" % "2.0.0" % Test,
  ws,
  cache,
  "com.pauldijou" %% "jwt-play" % "0.12.1",
  "org.sedis" %% "sedis" % "1.2.2",
  "org.mockito" % "mockito-core" % "1.8.5"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "com.example.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "com.example.binders._"
