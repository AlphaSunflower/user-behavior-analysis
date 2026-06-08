name := "KafkaProducerApp"
version := "1.0"
scalaVersion := "2.12.17"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "2.6.0"
)

run / javaOptions ++= Seq(
  "-Dfile.encoding=UTF-8",
  "-Dsun.stdout.encoding=UTF-8",
  "-Dsun.stderr.encoding=UTF-8"
)

fork := true
