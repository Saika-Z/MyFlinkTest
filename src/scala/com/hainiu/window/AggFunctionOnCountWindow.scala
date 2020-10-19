package com.hainiu.window

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object AggFunctionOnCountWindow {
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
    //env.setParallelism(1)
    input
      .keyBy(1)
      .countWindow(2)
      .aggregate(new SumAggregate)
      .print()
    env.execute()

  }
  }
class SumAggregate extends AggregateFunction[(String, String, String, Int),(String,Long),(String,Long)]{
  /**
   * 创建累加器来保存中间状态(name和count)
   */
  override def createAccumulator(): (String, Long) = {

    ("",0)

  }

  override def add(value: (String, String, String, Int), accumulator: (String, Long)): (String, Long) = {
    (s"${value._3}\t${accumulator._1}",accumulator._2 + value._4)
  }

  override def getResult(accumulator: (String, Long)): (String, Long) = {
    accumulator
  }

  override def merge(a: (String, Long), b: (String, Long)): (String, Long) = {
    (s"${a._1}\t${b._1}",a._2+b._2)
  }
}