package com.hainiu.window

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger

object JoinOnSessionWindow {
  import org.apache.flink.api.scala._
  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")
    //conf.setString("taskmanager.numberOfTaskSlots","2")

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    val input1 = env.socketTextStream("localhost", 6666, '\n')
    val map1: DataStream[(String, Int)] = input1.flatMap(_.split(" ")).map((_,1))

    val input2 = env.socketTextStream("localhost", 7777, '\n')
    val map2: DataStream[(String, Int)] = input2.flatMap(_.split(" ")).map((_,1))

    map1.join(map2)
      .where(_._1)
      .equalTo(_._1)
      .window(ProcessingTimeSessionWindows.withGap(Time.seconds(10)))
      .trigger(CountTrigger.of(1))
      .apply((a,b) => {
        s"${a._1}==${b._1}"
      }).print()

    env.execute()
  }
}