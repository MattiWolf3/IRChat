package com.volkova.controllers

import com.volkova.actors.User
import com.volkova.Global
import javafx.fxml.FXML
import javafx.scene.control.{Label, TextField}
import javafx.scene.paint.Color
import javafx.stage.Stage

/**
 * Отвечает за аутентификацию.
 */

class LoginController {

  @FXML
  private var userTextField: TextField = _

  @FXML
  private var messageLabel: Label = _

  @FXML
  def handleSignIn(): Unit = {
    val username = userTextField.getText.trim

    if (username.nonEmpty) {
      if (Global.memberMap.contains(username)) {
        messageLabel.setTextFill(Color.FIREBRICK)
        messageLabel.setText(s"Пользователь с именем $username уже существует.")
      } else {
        Global.username = username
        Global.system ! User.Subscribe(username)
        val stage = userTextField.getScene.getWindow.asInstanceOf[Stage]
        stage.close()
      }
    } else {
      messageLabel.setTextFill(Color.FIREBRICK)
      messageLabel.setText("Пожалуйста, введите имя пользователя.")
    }
  }
}
