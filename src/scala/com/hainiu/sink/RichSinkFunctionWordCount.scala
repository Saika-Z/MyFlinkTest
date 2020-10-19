package com.hainiu.sink

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

class RichFlinkSink extends RichSinkFunction[(String, Int)] {
  override def invoke(value: (String, Int), context: SinkFunction.Context[_]): Unit = {
    println(s"value:${value}," +
      s"processTime:${context.currentProcessingTime()}" +
      s"waterMark:${context.currentWatermark()}")

  }

  override def open(parameters: Configuration): Unit = {
    println("open")
  }

  override def close(): Unit = {
    println("close")
  }
}

object RichSinkFunctionWordCount {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.api.scala._
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")
    //conf.setString("taskmanager.numberOfTaskSlots","2")
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    env.setParallelism(1)
    val input: DataStream[String] = env.fromElements("Dick Dick Dick")

    val out: DataStream[(String, Int)] = input.flatMap(f => f.split(" ").map((_, 1))).keyBy(_._1).sum(1)

    //out.addSink(new RichFlinkSink)
    out.map(f => s"${f._1}\t${f._2}\n").writeToSocket("localhost", 7777,new SimpleStringSchema())
    env.execute("Connection")
  }

}
