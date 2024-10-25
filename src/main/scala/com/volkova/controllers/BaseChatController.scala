package com.volkova.controllers

import javafx.application.Platform
import javafx.fxml.FXML
import javafx.scene.control.{TextArea, TextField}

/**
 * Содержит общие поля и метод чатовых контроллеров.
 */

trait BaseChatController {

  @FXML
  protected var txtAreaDisplay: TextArea = _

  @FXML
  protected var txtInput: TextField = _

  def appendMessage(message: String): Unit = {
    Platform.runLater(() => {
      txtAreaDisplay.appendText(message + "\n")
    })
  }
}
