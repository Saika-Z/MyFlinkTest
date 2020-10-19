package com.hainiu.window

import org.apache.flink.configuration.{ConfigConstants, Configuration}
import org.apache.flink.streaming.api.scala.{DataStream, StreamExecutionEnvironment}
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.ProcessingTimeSessionWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.triggers.CountTrigger
import org.apache.flink.streaming.api.windowing.windows.GlobalWindow
import org.apache.flink.util.Collector

import scala.collection.mutable.ListBuffer

object CoGroupOnSessionWindow {
  import org.apache.flink.api.scala._
  def main(args: Array[String]): Unit = {
    val conf = new Configuration()
    conf.setBoolean(ConfigConstants.LOCAL_START_WEBSERVER, true)

    conf.setString("web.log.path", "C://tmp/flink_log")
    //conf.setString("taskmanager.numberOfTaskSlots","2")

    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.createLocalEnvironmentWithWebUI(conf)
    val input1: DataStream[String] = env.socketTextStream("localhost", 6666, '\n')
    val map1: DataStream[(String, Int)] = input1.flatMap(_.split(" ")).map((_, 1))

    val input2: DataStream[String] = env.socketTextStream("localhost", 7777, '\n')
    val map2: DataStream[(String, Int)] = input2.flatMap(_.split(" ")).map((_, 1))
    /**
     * 1、创建两个socket stream。输入的字符串以空格为界分割成Array[String]。然后再取出其中前两个元素组成(String, String)类型的tuple。
     * 2、join条件为两个流中的数据((String, String)类型)第一个元素相同。
     * 3、为测试方便，这里使用session window。只有两个元素到来时间前后相差不大于10秒之时才会被匹配。
     * Session window的特点为，没有固定的开始和结束时间，只要两个元素之间的时间间隔不大于设定值，就会分配到同一个window中，否则后来的元素会进入新的window。
     * 4、将window默认的trigger修改为count trigger。这里的含义为每到来一个元素，都会立刻触发计算。
     * 5、由于设置的并行度为12，所以有12个task
     * 6、所以两边相同的key会跑到其中一个task中，这样才能达到join的目的
     *    但是由于使用的是cogroup所以两边流跑到一个task中的key无论能不能匹配，都会以执行打印
     *    不能匹配的原因可能其中一个流相同的那个key还没有发送过来
     *
     */

    map1.coGroup(map2)
        .where(_._1)
        .equalTo(_._1)
        .window(ProcessingTimeSessionWindows.withGap(Time.seconds(10)))
        .trigger(CountTrigger.of(1))
        .apply((a,b,o:Collector[String]) => {
          val list: ListBuffer[String] = ListBuffer[String]("Data in stream1: \n")
          a.foreach(f => list += s"${f._1}<->${f._2}\n")
          list += "Data in stream2:\n"
          b.foreach(f => list += s"${f._1}<->${f._2}\n")
          o.collect(list.reduce(_+_))
        }).print()

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

