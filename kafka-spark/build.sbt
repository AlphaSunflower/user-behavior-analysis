import sbtassembly.{MergeStrategy, PathList}

name := "UserBehaviorAnalysis"
version := "1.0"
scalaVersion := "2.12.17"

libraryDependencies ++= Seq(
  // Spark Streaming 核心依赖
  "org.apache.spark" %% "spark-streaming" % "3.4.0" % "compile",   // 本地开发用 compile，部署时改回 provided
  // Spark 与 Kafka 集成依赖（0-10 版本）
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.4.0",
  // MySQL 连接驱动
  "mysql" % "mysql-connector-java" % "5.1.49",
  // JSON 解析
  "com.alibaba" % "fastjson" % "1.2.83",
  // Spark SQL（用于 DataFrame 操作，可选）
  "org.apache.spark" %% "spark-sql" % "3.4.0" % "compile"   // 本地开发用 compile，部署时改回 provided
)

// JDK 17+ 运行 Spark 所需的 JVM 模块开放参数
run / javaOptions ++= Seq(
  "-Dfile.encoding=UTF-8",
  "-Dsun.stdout.encoding=UTF-8",
  "-Dsun.stderr.encoding=UTF-8",
  "--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
  "--add-opens=java.base/java.nio=ALL-UNNAMED",
  "--add-opens=java.base/java.lang=ALL-UNNAMED",
  "--add-opens=java.base/java.lang.invoke=ALL-UNNAMED",
  "--add-opens=java.base/java.util=ALL-UNNAMED",
  "--add-opens=java.base/java.io=ALL-UNNAMED",
  "--add-opens=java.base/sun.security.action=ALL-UNNAMED",
  "-Dspark.master=local[*]"
)

fork := true

// 打包配置
assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case _ => MergeStrategy.first
}

// 避免与 provided 依赖冲突
assembly / assemblyOption := (assembly / assemblyOption).value
  .withIncludeScala(false)
