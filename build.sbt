name := """ideas"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayScala)

scalaVersion := "2.11.7"

val webJars = Seq(
    "org.webjars" %% "webjars-play" % "2.5.0-2",
    "org.webjars" % "bootstrap" % "3.3.7-1"
)

libraryDependencies ++= webJars ++ Seq(
    "com.mohiva" %% "play-silhouette" % "4.0.0",
    "com.mohiva" %% "play-silhouette-crypto-jca" % "4.0.0",
    "com.mohiva" %% "play-silhouette-password-bcrypt" % "4.0.0",
    "com.mohiva" %% "play-silhouette-persistence" % "4.0.0",
    "com.typesafe.play" %% "play-slick" % "2.0.2",
    "com.typesafe.play" %% "play-slick-evolutions" % "2.0.2",
    "org.postgresql" % "postgresql" % "9.4.1208",
    "com.nulab-inc" %% "play2-oauth2-provider" % "0.18.0",
    "net.codingwell" %% "scala-guice" % "4.0.1",
    "com.iheart" %% "ficus" % "1.2.6",
    "org.scalatestplus.play" %% "scalatestplus-play" % "1.5.1" % Test
)

resolvers += Resolver.jcenterRepo

resolvers += "scalaz-bintray" at "http://dl.bintray.com/scalaz/releases"

