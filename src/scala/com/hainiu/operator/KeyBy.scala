package com.hainiu.operator

import org.apache.flink.api.java.tuple.Tuple
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{DataStream, KeyedStream, StreamExecutionEnvironment}

object KeyBy {
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

    //  val keyBy = input.keyBy(1)
    //    val keyBy = input.keyBy(new KeySelector[(String, String, String, Int), String] {
    //    override def getKey(in: (String, String, String, Int)) = {
    //      in._2
    //        }
    //    })
    val keyBy = input.keyBy(_._2)

    val max = keyBy.maxBy("_4")

    max.print()
    val tuple1 = List(
      EventKey("Dick", "row1", "Van", 33),
      EventKey("Dick", "row1", "Billy", 30),
      EventKey("Dick", "row2", "DeepDarkFantasy", 34),
      EventKey("Dick", "row2", "BigDick", 32))


    val tupleinput :DataStream[EventKey]= env.fromCollection(tuple1)


    val tkeyBy:KeyedStream[EventKey,Tuple] = tupleinput.keyBy("b")

    tkeyBy.maxBy("d").print()
    tkeyBy.map(f => new EnentValue(f.a,f.b,f.c,f.d)).print("a")
    env.execute("key")

  }
}

case class EventKey(a: String, b: String, c: String, d: Int)

class EnentValue(a: String, b: String, c: String, d: Int)