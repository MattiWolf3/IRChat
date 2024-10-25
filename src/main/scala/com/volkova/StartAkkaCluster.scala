package com.volkova

import akka.actor.{Address, AddressFromURIString}
import akka.actor.typed.ActorSystem
import akka.cluster.typed.{Cluster, JoinSeedNodes}
import com.typesafe.config.{Config, ConfigFactory}
import com.volkova.actors.User
import scala.jdk.CollectionConverters._

/**
 * Отвечает за инициализацию и настройку акторной системы.
 */

object StartAkkaCluster {
  def startup(ip: String, seedPort1: Int, seedPort2: Int, port: Int): Unit = {
    // Задание конфигурации
    val overrides: Map[String, Object] = Map(
      "akka.remote.artery.canonical.hostname" -> ip,
      "akka.remote.artery.canonical.port" -> port.asInstanceOf[AnyRef]  // Приводим Int к AnyRef
    )

    val config: Config = ConfigFactory.parseMap(overrides.asJava).withFallback(ConfigFactory.load())

    /** Акторная система с корневым актором User */
    val system: ActorSystem[User.Command] = ActorSystem(User(), "ClusterSystem", config)
    Global.system = system

    val address1 = s"akka://ClusterSystem@$ip:$seedPort1"
    val address2 = s"akka://ClusterSystem@$ip:$seedPort2"

    val seedNodes: List[Address] = List(address1, address2).map(AddressFromURIString.parse)

    // Подключение к кластеру
    Cluster(system).manager ! JoinSeedNodes(seedNodes)
    println(s"StartAkkaCluster: Joined seed nodes: $seedNodes")
  }
}
