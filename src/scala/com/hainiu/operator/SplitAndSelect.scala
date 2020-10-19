package com.hainiu.operator

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{DataStream, SplitStream, StreamExecutionEnvironment}

import scala.collection.mutable.ListBuffer

object SplitAndSelect {
  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")
    //conf.setString("taskmanager.numberOfTaskSlots","2")
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    val input: DataStream[Long] = env.generateSequence(0, 10)

    val value: SplitStream[Long] = input.split(f => {
      val out: ListBuffer[String] = new ListBuffer[String]
      if (f % 2 == 0) {
        out += "Dick"
      } else {
        out += "Van"
      }
      out
    })
    value.select("Van")print()
    env.execute("Connection")
  }

}
