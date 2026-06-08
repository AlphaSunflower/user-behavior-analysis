name := "KafkaProducerApp"
version := "1.0"
scalaVersion := "2.12.17"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "3.3.2"
)

run / javaOptions ++= Seq(
  "-Dfile.encoding=UTF-8",
  "-Dsun.stdout.encoding=UTF-8",
  "-Dsun.stderr.encoding=UTF-8",
  "-Djava.net.preferIPv4Stack=true"
)

fork := true
