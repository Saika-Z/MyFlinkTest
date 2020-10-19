package com.hainiu.operator

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment}

object ReduceFoldAggregation {
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
      ("Dick", "row2", "BigDick", 32))
    import org.apache.flink.api.scala._
    val input = env.fromCollection(tuple)

    val map:DataStream[(String,Int)] = input.map(f => (f._2, 1))
    val keyBy:KeyedStream[(String,Int),String] = map.keyBy(_._1)

    val reduce = keyBy.reduce((a, b) => (a._1, a._2 + b._2))

    reduce.print()
    val fold:DataStream[(String,Int)] = keyBy.fold(("",0))((a,b) => (b._1,a._2+b._2))
    fold.print()

    env.execute("Aggregation")
  }
}
