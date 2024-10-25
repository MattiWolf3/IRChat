package com.volkova.actors

/**
 * Enum, представляющий уникальные имена акторов.
 */

object ActorType extends Enumeration {
  type ActorType = Value
  val Chat, UserListener, UserRef, Member = Value
}
