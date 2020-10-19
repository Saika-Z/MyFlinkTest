package com.hainiu.watermarkers

import com.hainiu.source.Source
import org.apache.flink.api.common.functions.FlatMapFunction
import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.util.Collector

class SourceWater2 extends SourceFunction[String] {

  var num = 0
  var isCancel = true

  override def run(sourceContext: SourceFunction.SourceContext[String]): Unit = {
//    while (true) {
//      sourceContext.collect(s"${num}")
//      Thread.sleep(1000)
//      num += 1
//      sourceContext.emitWatermark(new Watermark(System.currentTimeMillis()))
//
//    }
    while (true) {
      Thread.sleep(1000)
      val record: String = s"${num}_${System.currentTimeMillis()}"
      sourceContext.collect(record)
      val strings: Array[String] = record.split("_")
      num += 1
      val eventTime: Long = strings(2).toLong
      sourceContext.collectWithTimestamp(record,eventTime)
      sourceContext.emitWatermark(new Watermark(System.currentTimeMillis()))

    }
  }

  override def cancel(): Unit = {
    println("cancel")
    isCancel = false
  }
}
  object SourceWaterMarkers {
  import org.apache.flink.api.scala._
  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    val text = env.addSource(new Source)
    val value1 = text.flatMap(new FlatMapFunction[String, (String, Int)] {
      override def flatMap(t: String, collector: Collector[(String, Int)]) = {
        val strings: Array[String] = t.split(" ")
        for (s <- strings) {
          collector.collect((s, 1))
        }
      }
    })

    val wordcount: DataStream[(String, Int)] = value1.keyBy(0).sum(1)

    wordcount.print()

    env.execute("Dick")
  }


}
