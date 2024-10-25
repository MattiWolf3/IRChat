package com.volkova.controllers

import com.volkova.actors.User
import com.volkova.Global
import javafx.event.ActionEvent
import javafx.fxml.FXML

/**
 * Управляет пользовательским интерфейсом приватного чата.
 */

class PrivateChatController extends BaseChatController {

  /** Имя собеседника */
  private var interlocutor: String = _

  def setUsername(interlocutor: String): Unit = {
    this.interlocutor = interlocutor
  }

  @FXML
  def handleSend(event: ActionEvent): Unit = {
    val message = txtInput.getText.trim

    if (message.nonEmpty && Global.username != null && interlocutor != null) {
      Global.memberMap.get(interlocutor).foreach { userActorRef =>
        userActorRef ! User.PrivateMessage(message, interlocutor, Global.username)
      }
      // Отображение сообщения в собственном окне
      appendMessage(s"[${Global.username}]: $message")
      txtInput.clear()
    }
  }
}
