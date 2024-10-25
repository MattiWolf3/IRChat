package com.volkova.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import Chat._
import com.volkova.controllers.ChatController
import com.volkova.serialization.CBORSerializable
import javafx.application.Platform

/**
 * Управляет логикой общего чата и распространением сообщений между участниками.
 */

object Chat {

  sealed trait Command extends CBORSerializable
  final case object Subscribe extends Command
  final case class BroadcastMessage(message: String, username: String) extends Command
  final case class Message(message: String, username: String) extends Command

  def apply(chatController: ChatController): Behavior[Command] =
    Behaviors.setup { context =>
      new Chat(context, chatController)
    }
}

class Chat(context: ActorContext[Command], chatController: ChatController) extends AbstractBehavior[Command](context) {

  /** Топик, в котором публикуются сообщения */
  private val topic: ActorRef[Topic.Command[Message]] =
    context.spawn(Topic[Message]("chat"), ActorType.Chat.toString)

  override def onMessage(message: Command): Behavior[Command] = {
    message match {
      // Подписка на чат
      case Subscribe =>
        topic ! Topic.Subscribe(context.self)
        Behaviors.same

      // Рассылка сообщения всем участникам
      case BroadcastMessage(message, name) =>
        topic ! Topic.Publish(Message(message, name))
        Behaviors.same

      // Отображение сообщения в чате
      case Message(message, name) =>
        Platform.runLater(() => {
          chatController.appendMessage(s"[$name]: $message")
        })
        Behaviors.same
    }
  }
}
