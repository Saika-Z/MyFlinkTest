package com.hainiu.sink

import org.apache.flink.api.common.functions.Partitioner
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object CustomPartitioner {
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
    input.partitionCustom(new FlickPartitioner,_._2).print()

    env.execute("Connection")
  }
}
class FlickPartitioner extends Partitioner[String]{
  override def partition(k: String, i: Int): Int = {
    print(k)
    1
  }
}