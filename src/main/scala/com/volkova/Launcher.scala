package com.volkova

import javafx.application.Application
import javafx.stage.Stage

object Launcher extends App {
  Application.launch(classOf[Launcher], args: _*)
}

class Launcher extends Application {

  override def start(primaryStage: Stage): Unit = {
    UIManager.initializeChatFrame()
    UIManager.initializeLoginFrame(primaryStage)

    // Извлечение аргументов, переданных приложению при запуске
    val params = getParameters.getRaw.toArray
    if (params.length != 4) {
      println("Wrong input params. Use next command instead: ./gradlew run --args=\"127.0.0.1 25251 25252 port\"")
      System.exit(1)
    }

    StartAkkaCluster.startup(
      params(0).toString,       //ip
      params(1).toString.toInt, //seedPort1
      params(2).toString.toInt, //seedPort2
      params(3).toString.toInt  //port
    )
  }

  override def stop(): Unit = {
    if (Global.system != null) {
      Global.system.terminate()
    }
  }
}
