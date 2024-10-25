package com.volkova.controllers

import com.volkova.actors.User
import com.volkova.Global
import javafx.application.Platform
import javafx.collections.{FXCollections, ObservableList}
import javafx.event.ActionEvent
import javafx.fxml.FXML
import javafx.scene.control.ListView
import javafx.scene.input.MouseEvent

/**
 * Управляет UI группового чата и взаимодействует с акторной системой.
 */

class ChatController extends BaseChatController {

  /** Прокручиваемый список участников */
  @FXML
  protected var participantsListView: ListView[String] = _

  /** Список, который позволяет UserListener отслеживать изменения */
  private val participants: ObservableList[String] = FXCollections.observableArrayList()

  // Вызывается автоматически при загрузке FXML
  @FXML
  def initialize(): Unit = {
    Platform.runLater(() => {
      // Добавление текущих участников из memberMap
      participants.addAll(Global.memberMap.keySet.toSeq: _*)
      // Отображение текущих участников
      participantsListView.setItems(participants)
    })
  }

  @FXML
  def handleSend(event: ActionEvent): Unit = {
    val message = txtInput.getText.trim

    if (message.nonEmpty && Global.username != null) {
      Global.system ! User.Message(message, Global.username)
      txtInput.clear()
    }
  }

  @FXML
  def handleParticipantClick(event: MouseEvent): Unit = {
    if (event.getClickCount == 2) {
      val selectedUser = participantsListView.getSelectionModel.getSelectedItem
      if (selectedUser != null && selectedUser != Global.username) {
        Global.system ! User.SubscribePrivateChat(selectedUser)
      }
    }
  }

  def updateParticipantList(username: String, add: Boolean): Unit = {
    Platform.runLater(() => {
      if (!participants.contains(username) && add ) {
        participants.add(username)
      } else {
        participants.remove(username)
      }
    })
  }
}
