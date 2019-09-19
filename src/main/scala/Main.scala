object Main {
  def main(args: Array[String]): Unit = {
    args match {
      case Array() =>
      case _ =>
        println(s"Incorrect parameters: [${args.mkString(", ")}]")
        System.exit(1)
    }
  }
}