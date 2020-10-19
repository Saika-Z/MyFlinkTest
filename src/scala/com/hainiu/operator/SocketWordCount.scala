package com.hainiu.operator

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.util.Collector

object SocketWordCount {
  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER,true)
    val env = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    val text:DataStream[String] = env.socketTextStream("localhost", 6666)
    env.setParallelism(2)
    import org.apache.flink.api.scala._
    //val wordcount:DataStream[(String,Int)] = text.flatMap(_.split(" ")).map((_, 1)).keyBy(_._1).sum(1)
    val wordcount:DataStream[(String,Int)] = text.flatMap(new FlatMapFunction[String,(String,Int)] {
      override def flatMap(value:String,out:Collector[(String,Int)]) = {
        val strings: Array[String] = value.split(" ")
        for (s <- strings) {
          out.collect((s, 1))
        }
      }
    }).setParallelism(2).keyBy(_._1).sum(1)
    wordcount.filter(_._2 > 1)
    wordcount.print().setParallelism(2)

    env.execute("bigdick")
  }

}
