package com.volkova.serialization

/**
 * Маркерный трейт.
 * В Akka Cluster сообщения между акторами, находящимися на разных нодах, должны сериализовываться для передачи
 * сообщений через сеть.
 * CBOR (Concise Binary Object Representation) — это двоичный формат для представления данных, который предлагает
 * высокую производительность и компактность в таких системах, как Akka Cluster.
 */

trait CBORSerializable {}
