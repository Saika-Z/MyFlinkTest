package com.hainiu.sink

import org.apache.flink.api.common.io.OutputFormat
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

class OutPut extends OutputFormat[(String, Int)] {
  override def configure(configuration: Configuration): Unit = {
    println("config")
  }

  override def open(i: Int, i1: Int): Unit = {
    println(s"taskNumber:${i},numTasks:${i1}")
  }

  override def writeRecord(it: (String, Int)): Unit = {
    println("record")
  }

  override def close(): Unit = {
    println("close")
  }
}

object OutputFormatWordCount {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.api.scala._
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")
    //conf.setString("taskmanager.numberOfTaskSlots","2")
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    env.setParallelism(1)
    //val input: DataStream[String] = env.fromElements("Dick Dick Dick")
    val input: DataStream[String] = env.socketTextStream("localhost", 6666, '\n')
    val out: DataStream[(String, Int)] = input.flatMap(f => f.split(" ").map((_, 1))).keyBy(_._1).sum(1)

   // out.writeUsingOutputFormat(new OutPut)
    out.map(f => s"${f._1}\t${f._2}\n").writeToSocket("localhost", 7777,new SimpleStringSchema())
    env.execute("Connection")
  }

}
