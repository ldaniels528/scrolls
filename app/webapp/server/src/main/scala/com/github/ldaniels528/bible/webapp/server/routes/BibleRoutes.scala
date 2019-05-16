package com.github.ldaniels528.bible.webapp.server.routes

import com.github.ldaniels528.bible.webapp.server.dao.BibleDAO
import io.scalajs.npm.express.{Application, Request, Response}

import scala.concurrent.ExecutionContext
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Bible Routes
  * @author lawrence.daniels@gmail.com
  */
class BibleRoutes(app: Application)(implicit ec: ExecutionContext, bibleDAO: BibleDAO) {

  // define the API
  app.get("/api/:book/:chapter", (request: Request, response: Response, next: NextFunction) => lookupChapter(request, response, next))
  app.get("/api/:book/:chapter/:verse", (request: Request, response: Response, next: NextFunction) => lookupVerse(request, response, next))
  app.get("/api/:book/:chapter/:fromVerse/:toVerse", (request: Request, response: Response, next: NextFunction) => lookupVerses(request, response, next))

  def lookupChapter(request: Request, response: Response, next: NextFunction): Unit = {
    val book = request.params("book")
    val chapterNo = request.params("chapter").toInt
    bibleDAO.find(book, chapterNo) onComplete {
      case Success(Nil) => response.notFound(); next()
      case Success(verses) => response.send(verses.toJSArray); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def lookupVerse(request: Request, response: Response, next: NextFunction): Unit = {
    val book = request.params("book")
    val chapterNo = request.params("chapter").toInt
    val verseNo = request.params("verse").toInt
    bibleDAO.find(book, chapterNo, verseNo) onComplete {
      case Success(Some(verse)) => response.send(verse); next()
      case Success(None) => response.notFound(); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

  def lookupVerses(request: Request, response: Response, next: NextFunction): Unit = {
    val book = request.params("book")
    val chapterNo = request.params("chapter").toInt
    val fromVerse = request.params("fromVerse").toInt
    val toVerse = request.params("toVerse").toInt
    bibleDAO.find(book, chapterNo, fromVerse, toVerse) onComplete {
      case Success(Nil) => response.notFound(); next()
      case Success(verses) => response.send(verses.toJSArray); next()
      case Failure(e) => response.internalServerError(e); next()
    }
  }

}
