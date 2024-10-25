package com.volkova.actors

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.pubsub.Topic
import akka.actor.typed.scaladsl.{AbstractBehavior, ActorContext, Behaviors}
import akka.cluster.ClusterEvent._
import akka.cluster.typed.{Cluster, Subscribe => ClusterSubscribe}
import com.volkova.{Global, UIManager}
import com.volkova.serialization.CBORSerializable
import scala.concurrent.duration._
import UserListener._

/**
 * Отвечает за кластерные события.
 */

object UserListener {

  sealed trait Event extends CBORSerializable
  final case class BroadcastUserRef(actorRef: ActorRef[User.Command], name: String) extends Event
  private final case class MemberChange(event: MemberEvent) extends Event
  private final case class SaveUserRef(actorRef: ActorRef[User.Command], name: String) extends Event
  private final case class ManageMembers(memberMap: Map[String, ActorRef[User.Command]]) extends Event

  def apply(): Behavior[Event] =
    Behaviors.setup { context =>
    new UserListener(context)
  }
}

class UserListener(context: ActorContext[UserListener.Event])
  extends AbstractBehavior[UserListener.Event](context) {

  /** Топик для управления ссылками на конкретных пользователей */
  private val topicRef: ActorRef[Topic.Command[SaveUserRef]] =
    context.spawn(Topic[SaveUserRef]("userRef"), ActorType.UserRef.toString)
  topicRef ! Topic.Subscribe(context.self)

  /** Топик, который отвечает за информацию о присутствии пользователей в чате */
  private val topicMember: ActorRef[Topic.Command[ManageMembers]] =
    context.spawn(Topic[ManageMembers]("member"), ActorType.Member.toString)
  topicMember ! Topic.Subscribe(context.self)

  /** Адаптер, который преобразует сообщения типа MemberEvent в сообщение типа MemberChange */
  private val memberEventAdapter: ActorRef[MemberEvent] = context.messageAdapter(MemberChange)

  // Подписка актора UserListener на события кластера
  Cluster(context.system).subscriptions ! ClusterSubscribe(memberEventAdapter, classOf[MemberEvent])

  override def onMessage(message: Event): Behavior[Event] = {
    message match {
      // Рассылка информацию о новом участнике всем другим
      case BroadcastUserRef(actorRef, username) =>
        context.scheduleOnce(2.seconds, topicRef, Topic.Publish(SaveUserRef(actorRef, username)))
        Behaviors.same

      // Сохранение ссылки на нового пользователя
      case SaveUserRef(actorRef, username) =>
        Global.memberMap.put(username, actorRef)
        UIManager.chatController.foreach(_.updateParticipantList(username, add = true))
        Behaviors.same

      // Изменение статуса участника в кластере
      case MemberChange(changeEvent) =>
        changeEvent match {
          case MemberUp(member) =>
            context.scheduleOnce(2.seconds, topicMember, Topic.Publish(ManageMembers(Global.memberMap.toMap)))
            context.log.info(s"Member is Up: ${member.address}")

          case MemberRemoved(member, previousStatus) =>
            context.log.info(s"Member is Removed: ${member.address} after $previousStatus")
            val removedUsernameOpt = Global.memberMap
              .find(_._2.path.address == member.address)  // Если адреса удаленной ноды и ActorRef'а совпадают
              .map(_._1)                                  // Извлекаем имя пользователя

            removedUsernameOpt.foreach { username =>
              Global.memberMap.remove(username)           // Удаляем его из memberMap
              UIManager.chatController.foreach(_.updateParticipantList(username, add = false)) // И обновляем UI
              context.log.info(s"Removed user '$username' from memberMap and updated UI")
            }

          case _: MemberEvent =>
        }
        Behaviors.same

      // Управление всей коллекцией участников
      case ManageMembers(memberMap) =>
        // Новый memberMap содержит больше участников => появились новые пользователи
        if (Global.memberMap.size < memberMap.size) {
          memberMap.foreach { case (username, ref) =>
            Global.memberMap.putIfAbsent(username, ref)
            UIManager.chatController.foreach(_.updateParticipantList(username, add = true))
          }
        }
        Behaviors.same
    }
  }
}
