package com.hainiu.operator

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.datastream.DataStreamUtils
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}

object SocketMap {
  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")
    //conf.setString("taskmanager.numberOfTaskSlots","2")

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    val text = env.socketTextStream("localhost", 6666, '\n')

    import org.apache.flink.api.scala._
    val windowcount:DataStream[(String,Int)] = text.flatMap(s => s.split(" ")).map(s=>(s,1))

    windowcount.print()
    import scala.collection.convert.wrapAll._
    val value = DataStreamUtils.collect(windowcount.javaStream)
    for (v <- value){
     println(v)
    }
    env.execute()
  }

}
