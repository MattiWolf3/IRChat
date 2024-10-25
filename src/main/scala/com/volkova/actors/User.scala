package com.volkova.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import com.volkova.controllers.PrivateChatController
import com.volkova.serialization.CBORSerializable
import com.volkova.UIManager
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.{Parent, Scene}
import javafx.stage.Stage
import scala.collection.concurrent.TrieMap
import User._

/**
 * Root guardian (базовый/корневой актор).
 */

object User {

  sealed trait Command extends CBORSerializable
  final case class Subscribe(username: String) extends Command
  final case class Message(message: String, username: String) extends Command
  final case class SubscribePrivateChat(interlocutor: String) extends Command
  final case class PrivateMessage(message: String, to: String, from: String) extends Command

  def apply(): Behavior[Command] =
    Behaviors.setup { context =>
      new User(context)
    }

  private val privateChatWindows: TrieMap[String, Stage] = TrieMap.empty

  private val privateChatControllers: TrieMap[String, PrivateChatController] = TrieMap.empty

  private def addPrivateChatController(interlocutor: String, controller: PrivateChatController): Unit = {
    privateChatControllers.put(interlocutor, controller)
  }

  private def getPrivateChatController(from: String): Option[PrivateChatController] = {
    privateChatControllers.get(from)
  }
}

class User(context: ActorContext[User.Command]) extends AbstractBehavior[User.Command](context) {

  /** Актор, который связывает логику чата с UI */
  private val chat: ActorRef[Chat.Command] = UIManager.chatController match {
    case Some(controller) => context.spawn(Chat(controller), ActorType.Chat.toString)
    case None => throw new IllegalStateException("ChatController is not initialized")
  }

  /** Актор, который наблюдает за состояниями пользователей в системе */
  private val userListener: ActorRef[UserListener.Event] =
    context.spawn(UserListener(), ActorType.UserListener.toString)

  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      case Subscribe(username) =>
        chat ! Chat.Subscribe
        userListener ! UserListener.BroadcastUserRef(context.self, username)
        Behaviors.same

      case SubscribePrivateChat(interlocutor) =>
        Platform.runLater(() => {
            privateChatWindows.get(interlocutor) match {
              case Some(existingStage) =>
                existingStage.toFront()
              case None =>
                val loader = new FXMLLoader(getClass.getResource("/view/private_chat.fxml"))
                val parent = loader.load[Parent]()
                val stage = new Stage()
                stage.setScene(new Scene(parent, 450, 500))
                stage.setTitle(s"Приватный чат с $interlocutor")
                stage.show()

                // Сохранение ссылки на новое окно в коллекции
                privateChatWindows.put(interlocutor, stage)

                // Закрытие и удаление окна из коллекции по завершении
                stage.setOnCloseRequest(_ => privateChatWindows.remove(interlocutor))

                val controller = loader.getController[PrivateChatController]
                controller.setUsername(interlocutor)
                addPrivateChatController(interlocutor, controller)
            }
        })
        Behaviors.same

      case Message(message, name) =>
        chat ! Chat.BroadcastMessage(message, name)
        Behaviors.same

      // Отображение сообщения собеседника
      case PrivateMessage(message, _, from) =>
        getPrivateChatController(from).foreach { controller =>
          controller.appendMessage(s"[$from]: $message")
        }
        Behaviors.same
    }
  }
}
