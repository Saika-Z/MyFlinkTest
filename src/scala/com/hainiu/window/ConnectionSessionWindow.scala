package com.hainiu.window

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.functions.co.CoFlatMapFunction
import org.apache.flink.streaming.api.scala.{ConnectedStreams, DataStream, StreamExecutionEnvironment}
import org.apache.flink.util.Collector

import scala.collection.mutable

object ConnectionSessionWindow {
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

    val value: ConnectedStreams[(String, Int), (String, Int)] = map1.connect(map2)

    value.flatMap(new CoFlatMapFunction[(String,Int),(String,Int)] {
      private val stringToInt = new mutable.HashMap[String, Int]()

      override def flatMap1(value: (String, Int), out: Collector[String]): Unit = {
        val key: String = value._1
        val i: Int = stringToInt.getOrElseUpdate(key, 1)
        out.collect(s"${key}"+i)
      }

      override def flatMap2(value: (String, Int), out: Collector[String]): Unit = {
        val key: String = value._1
        val i: Int = stringToInt.getOrElseUpdate(key, 1)
      }
    })
        env.execute()
  }
}
