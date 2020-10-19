package com.hainiu.sink

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.functions.sink.SinkFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}


class FlinkSink extends SinkFunction[(String,Int)]{
  override def invoke(value: (String, Int), context: SinkFunction.Context[_]): Unit = {
    println(s"value:${value},"+
    s"processTime:${context.currentProcessingTime()}"+
    s"waterMark:${context.currentWatermark()}")
  }
}

object SinkFunctionWordCount {
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

    out.addSink(new FlinkSink)
    env.execute("Connection")
  }
}

