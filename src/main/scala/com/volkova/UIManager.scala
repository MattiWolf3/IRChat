package com.volkova

import com.volkova.controllers.ChatController
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage

object UIManager {

  var chatController: Option[ChatController] = None

  def initializeChatFrame(): Unit = {
    val chatStage = new Stage()
    val chatLoader = new FXMLLoader(getClass.getResource("/view/group_chat.fxml"))
    val chatParent: Parent = chatLoader.load()
    val chatScene = new Scene(chatParent, 600, 500)
    chatStage.setTitle("Общий чат")
    chatStage.setScene(chatScene)
    chatStage.show()
    val loadedChatController = chatLoader.getController[ChatController]
    chatController = Some(loadedChatController)
  }

  def initializeLoginFrame(primaryStage: Stage): Unit = {
    val loginLoader = new FXMLLoader(getClass.getResource("/view/login.fxml"))
    val loginParent: Parent = loginLoader.load()
    val loginScene = new Scene(loginParent, 500, 300)
    primaryStage.setTitle("Вход в чат")
    primaryStage.setScene(loginScene)
    primaryStage.show()
  }
}
