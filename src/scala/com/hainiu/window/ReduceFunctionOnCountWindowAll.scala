package com.hainiu.window

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object ReduceFunctionOnCountWindowAll {
  import org.apache.flink.api.scala._
  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")
    //conf.setString("taskmanager.numberOfTaskSlots","2")

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    val tuple = List(
      ("Dick", "row1", "Van", 33),
      ("Dick", "row1", "Billy", 30),
      ("Dick", "row2", "DeepDarkFantasy", 34),
      ("Dick", "row1", "BigDick", 32))
    val input: DataStream[(String, String, String, Int)] = env.fromCollection(tuple)
    env.setParallelism(1)
    val windows: DataStream[(String, String, String, Int)] = input.keyBy(1).countWindowAll(2).reduce((a, b) => (a._1, a._2, a._3, a._4 + b._4))

    windows.print()

    env.execute()

  }

}
