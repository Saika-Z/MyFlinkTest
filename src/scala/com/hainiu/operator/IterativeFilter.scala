package com.hainiu.operator

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object IterativeFilter {
  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")
    //conf.setString("taskmanager.numberOfTaskSlots","2")

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    env.setParallelism(1)

    val input = env.generateSequence(0, 10)
    import org.apache.flink.api.scala._
    val value:DataStream[Long] = input.iterate(
      d => (d.map(_ - 1),
        d.filter(_ > 0)))

    value.print()


    env.execute("iter")
  }
}
