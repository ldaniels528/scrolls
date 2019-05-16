package com.github.ldaniels528.bible.webapp.server

import com.github.ldaniels528.bible.webapp.server.dao.BibleDAO
import com.github.ldaniels528.bible.webapp.server.routes.{NextFunction, _}
import com.github.ldaniels528.bible.webapp.server.util.LoggerFactory
import com.github.ldaniels528.bible.webapp.server.util.ProcessHelper._
import com.github.ldaniels528.bible.webapp.server.util.StringHelper._
import io.scalajs.nodejs.process
import io.scalajs.npm.bodyparser.{BodyParser, UrlEncodedBodyOptions}
import io.scalajs.npm.express.fileupload.ExpressFileUpload
import io.scalajs.npm.express.{Application, Express, Request, Response}
import io.scalajs.npm.expressws._

import scala.concurrent.ExecutionContext
import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
  * Bible Web Server
  * @author lawrence.daniels@gmail.com
  */
object BibleWebServerJsApp {
  private val logger = LoggerFactory.getLogger(getClass)

  @JSExport
  def main(args: Array[String]): Unit = {
    logger.info("Starting the ShockTrade Web Server...")

    // determine the port to listen on
    val startTime = js.Date.now()

    // setup the DAOs
    implicit val bibleDAO: BibleDAO = BibleDAO()

    // setup the application
    val port = process.port getOrElse "9000"
    val app = configureApplication()
    app.listen(port, () => logger.info(f"Server now listening on port $port [${js.Date.now() - startTime}%.1f msec]"))

    // handle any uncaught exceptions
    process.onUncaughtException { err =>
      logger.error("An uncaught exception was fired:")
      logger.error(err.stack)
    }
  }

  def configureApplication()(implicit ec: ExecutionContext, bibleDAO: BibleDAO): Application with WsRouting = {
    logger.info("Loading Express modules...")
    implicit val app: Application with WsRouting = Express().withWsRouting
    implicit val wss: WsInstance = ExpressWS(app)

    // setup the routes for serving static files
    logger.info("Setting up the routes for serving static files...")
    app.use(ExpressFileUpload())
    app.use(Express.static("public"))
    app.use("/bower_components", Express.static("bower_components"))

    // setup the body parsers
    logger.info("Loading Body Parser...")
    app.use(BodyParser.json())
      .use(BodyParser.urlencoded(new UrlEncodedBodyOptions(extended = true)))

    // disable caching
    app.disable("etag")

    // setup logging of the request - response cycles
    app.use((request: Request, response: Response, next: NextFunction) => {
      val startTime = js.Date.now()
      next()
      response.onFinish(() => {
        val elapsedTime = js.Date.now() - startTime
        val query = if (request.query.nonEmpty) (request.query map { case (k, v) => s"$k=$v" } mkString ",").limitTo(120) else "..."
        logger.info("[node] application - %s %s (%s) ~> %d [%d ms]", request.method, request.path, query, response.statusCode, elapsedTime)
      })
    })

    // setup web socket routes
    logger.info("Setting up web socket...")
    app.ws("/websocket", callback = (ws: WebSocket, request: Request) => {
      ws.onMessage(WebSocketHandler.messageHandler(ws, request, _))
    })

    // setup all other routes
    logger.info("Setting up all other routes...")
    new BibleRoutes(app)
    app
  }

}
