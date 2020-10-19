package com.hainiu.source

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.functions.source.{RichParallelSourceFunction, SourceFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.util.Collector

class RichSource extends RichParallelSourceFunction[String]{
  var num = 0
  var isCancel = true

  override def open(parameters: Configuration): Unit = {
    print("open")
    num = 100
  }
  override def run(sourceContext: SourceFunction.SourceContext[String]): Unit = {
    while (true){
      sourceContext.collect(s"N${num}")
      Thread.sleep(1000)
      num += 1
    }
  }


  override def close(): Unit = {
    print("close")
    num = 0
  }
  override def cancel(): Unit = {
    println("cancel")
    isCancel = false
  }
}

object RichParallelSourceFunction {
  def main(args: Array[String]): Unit = {
    import org.apache.flink.api.scala._
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")
    conf.setString("taskmanager.numberOfTaskSlots","2")

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    env.setParallelism(2)

    val text = env.addSource(new Source)
    val wordcount = text.flatMap(new FlatMapFunction[String, (String, Int)] {
      override def flatMap(t: String, collector: Collector[(String, Int)]) = {
        val strings: Array[String] = t.split(" ")
        for (s <- strings) {
          collector.collect((s, 1))
        }
      }
    }).keyBy(0)
      .sum(1)

    wordcount.print()

    env.execute("Dick")

  }

}
