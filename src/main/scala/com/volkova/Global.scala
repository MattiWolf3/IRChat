package com.volkova

import akka.actor.typed.{ActorRef, ActorSystem}
import com.volkova.actors.User
import scala.collection.concurrent.TrieMap

/**
 * Пространство для глобальных переменных.
 */

object Global {

  var username: String = _

  var system: ActorSystem[User.Command] = _

  /** Потокобезопасный Map, который хранит ссылки на акторов пользователей */
  val memberMap: TrieMap[String, ActorRef[User.Command]] = TrieMap.empty
}
