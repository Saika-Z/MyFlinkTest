package com.hainiu.source

import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.util.Collector

class Source extends SourceFunction[String] {
  var num = 0
  var isCancel = true

  override def run(sourceContext: SourceFunction.SourceContext[String]): Unit = {
    while (true) {
      sourceContext.collect(s"${num}")
      Thread.sleep(1000)
      num += 1
    }
  }

  override def cancel(): Unit = {
    println("cancel")
    isCancel = false
  }
}

object SourceFunctionWordCount {
  def main(args: Array[String]): Unit = {

    import org.apache.flink.api.scala._
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    val text = env.addSource(new Source)
    val wordcount = text.flatMap(new FlatMapFunction[String, (String, Int)] {
      override def flatMap(t: String, collector: Collector[(String, Int)]) = {
        val strings: Array[String] = t.split(" ")
        for (s <- strings) {
          collector.collect((s, 1))
        }
      }
    }).setParallelism(2).keyBy(0)
      .sum(1)

    wordcount.print()

    env.execute("Dick")
  }
}