import play.sbt.routes.RoutesKeys.{InjectedRoutesGenerator, routesGenerator}

organization := "io.goprime"
name := "backend-exercise"

ThisBuild / scalaVersion := "2.12.9"

ThisBuild / version := "1.0.0"

routesGenerator := InjectedRoutesGenerator

PlayKeys.devSettings += "config.resource" -> "development.conf"
fork in Test := true
javaOptions in Test ++= Seq("-Dconfig.file=conf/test.conf", "-Dlogger.resource=test.logback.xml")


sources in (Compile, doc) := Seq.empty
publishArtifact in (Compile, packageDoc) := false
libraryDependencies ++= Seq(
  guice,

  "junit" % "junit" % "4.12",
  "org.mongodb" % "mongodb-driver-sync" % "4.3.0",
  "org.projectlombok" % "lombok" % "1.18.12",
  "de.flapdoodle.embed" % "de.flapdoodle.embed.mongo" % "2.0.0",

  "org.mindrot" % "jbcrypt" % "0.3m",
  "org.hibernate" % "hibernate-validator" % "6.1.5.Final",
  "org.glassfish" % "javax.el" % "3.0.0",

  "com.auth0" % "java-jwt" % "3.3.0"
)
resolvers += "scalaz-bintray" at "https://dl.bintray.com/scalaz/releases"
resolvers += Resolver.url("Typesafe Ivy releases", url("https://repo.typesafe.com/typesafe/ivy-releases"))(Resolver.ivyStylePatterns)
resolvers += Resolver.typesafeRepo("releases")
resolvers += Resolver.sbtPluginRepo("releases")
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

lazy val root = (project in file(".")).enablePlugins(PlayScala)