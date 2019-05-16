package com.github.ldaniels528.bible.webapp.server

import java.util.UUID

import com.github.ldaniels528.bible.events.RemoteEvent
import com.github.ldaniels528.bible.webapp.server.util.LoggerFactory
import io.scalajs.nodejs.setTimeout
import io.scalajs.nodejs.timers.Timeout
import io.scalajs.npm.express.Request
import io.scalajs.npm.expressws.WebSocket
import io.scalajs.util.DurationHelper._

import scala.concurrent.duration._
import scala.scalajs.js
import scala.scalajs.js.JSON
import scala.util.{Failure, Success, Try}

/**
  * WebSocket Handler
  * @author Lawrence Daniels <lawrence.daniels@gmail.com>
  */
object WebSocketHandler {
  private val logger = LoggerFactory.getLogger(getClass)
  private val clients = js.Array[WsClient]()

  def messageHandler(ws: WebSocket, request: Request, message: String): Unit = {
    // handle the message
    message match {
      case "Hello" =>
        // have we received a message from this client before?
        val client = WsClient(ip = request.ip, ws = ws)
        logger.log(s"Client ${client.uid} (${client.ip}) connected")
        clients.push(client)
      case unknown =>
        logger.warn(s"Unhandled message '$message'...")
    }
  }

  def emit(action: String, data: String): Timeout = {
    setTimeout(() => {
      logger.log(s"Broadcasting action '$action' with data '$data'...")
      clients.foreach(client => Try(client.send(action, data)) match {
        case Success(_) =>
        case Failure(e) =>
          logger.warn(s"Client connection ${client.uid} (${client.ip}) failed")
          clients.indexWhere(_.uid == client.uid) match {
            case -1 => logger.error(s"Client ${client.uid} was not removed")
            case index => clients.remove(index)
          }
      })
    }, 0.seconds)
  }

  /**
    * Represents a web-socket client
    * @param ws the given [[WebSocket web socket]]
    */
  case class WsClient(ip: String, ws: WebSocket) {
    val uid: String = UUID.randomUUID().toString

    def send(action: String, data: String): Unit = ws.send(encode(action, data))

    private def encode(action: String, data: String) = JSON.stringify(new RemoteEvent(action, data))

  }

}
