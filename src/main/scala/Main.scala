object Main {
  def main(args: Array[String]): Unit = {
    args.toList match {
      case "-init" :: Nil =>
        Server.startWithInitialValues()
      case _ =>
        Server.start()
    }
  }
}