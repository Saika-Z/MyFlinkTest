package com.hainiu.window

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow
import org.apache.flink.util.Collector

object ProcessWinFunOnCountWindow {
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
      .keyBy(f => f._2)
      .countWindow(2)
      .process(new AvgProcessWindowFunction)
      .print()
    env.execute()
  }

}
class AvgProcessWindowFunction extends ProcessWindowFunction[(String, String, String, Int),String,String,GlobalWindow] {
  /**
   * 分组并计算windows里所有数据的平均值
   *
   * @param key        分组key
   * @param context    windows上下文
   * @param elements   分组的value
   * @param out        operator的输出结果
   */
  override def process(key: String, context: Context, elements: Iterable[(String, String, String, Int)], out: Collector[String]): Unit = {
    var sum = 0
    var count = 0
    for (in <- elements){
      sum += in._4
      count += 1
    }
    out.collect(s"Window:${context.window} count:${count} avg:${sum/count}")
  }
}