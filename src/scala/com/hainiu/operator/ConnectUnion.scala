package com.hainiu.operator

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{ConnectedStreams, DataStream, StreamExecutionEnvironment}
import org.apache.flink.util.Collector

object ConnectUnion {
  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")
    //conf.setString("taskmanager.numberOfTaskSlots","2")

    import org.apache.flink.api.scala._
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    val input1: DataStream[Long] = env.generateSequence(0, 10)
    val input2: DataStream[String] = env.fromCollection(List("Dick Deep Dark Fantasy"))

    val connetIn: ConnectedStreams[Long, String] = input1.connect(input2)

    val connect: DataStream[String] = connetIn.map[String](
      (a: Long) => a.toString,
      (b: String) => b
    )
    connect.print()


    val value: DataStream[String] = connetIn.flatMap[String](
      (in: Long, out: Collector[String]) => {
        out.collect(in.toString)
      },
      (in: String, out: Collector[String]) => {
        val strings: Array[String] = in.split(" ")
        for (s <- strings) {
          out.collect(s)
        }
      }
    )

    value.print()
    val input3: DataStream[Long] = env.generateSequence(11, 20)
    val input4: DataStream[Long] = env.generateSequence(21, 30)
    input1.union(input3).union(input4).print()
    env.execute("Connection")
  }

}
