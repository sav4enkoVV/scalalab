import org.apache.spark.sql.SparkSession

import scala.util.matching.Regex


object Main {
  def main(args: Array[String]): Unit = {
    println("Hello, I'm TR02!")

    //val inpath = "C:\\Users\\valera\\IdeaProjects\\scalalab\\lab-tr02\\src\\main\\scala\\Simple.html"

    val keyValPattern: Regex = "^([\\w]+)=(.+)$".r
    val map = args.map(arg => {
      val keyVal = keyValPattern.findPrefixMatchOf(arg).getOrElse(throw new RuntimeException("not valid arg"))
      (keyVal.group(1), keyVal.group(2))
    }).toMap

    println(s"input data: $map")

    val inpath = map.get("inpath").getOrElse(throw new RuntimeException("not set variable 'inpath'"))
    //val mask = map.get("mask").getOrElse(throw new RuntimeException("not set variable 'mask'"))
    //val outpath = map.get("outpath").getOrElse(throw new RuntimeException("not set variable 'outpath'"))

    transformation(inpath)
  }

  def transformation(inpath: String) = {

    val spark = SparkSession
      .builder()
      .master("local[*]")
      .appName("Spark TR02")
      .getOrCreate()

    import spark.sqlContext.implicits._

    val sc = spark.sparkContext

    val readFile = sc.textFile(inpath)

    val words = readFile
      .flatMap(lines => lines.split("\\s.*?\\s"))
      .filter(word => word.matches("\\<.*?\\>"))
      //.filter(word => word.matches("\\<[^<>]+\\>"))
      //.filter(word => word.matches(".*\\S.*"))
    val counts = words
      .map(w => (w, 1))
      .reduceByKey(_ + _)

    val columns = Seq("Tag", "Count")

    val df = counts.toDF(columns: _*)

    //df.coalesce(1).write.option("header", true).mode("append").csv("file:///C:/Users/valera/Downloads/htmls")

    df.show()
    df.printSchema()

    spark.stop()


  }
}
