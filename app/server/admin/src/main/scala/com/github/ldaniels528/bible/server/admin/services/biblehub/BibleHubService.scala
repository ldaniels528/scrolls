package com.github.ldaniels528.bible.server.admin.services.biblehub

import com.github.ldaniels528.bible.server.admin.services.{BibleService, BibleVerse}
import com.github.ldaniels528.bible.webapp.server.util.LoggerFactory
import io.scalajs.npm.request.Request

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Bible Hub Service
  * @author lawrence.daniels@gmail.com
  */
object BibleHubService extends BibleService with BibleHubHtmlParser {
  private[this] val logger = LoggerFactory.getLogger(getClass)

  /**
    * Returns the promise of a collection of a Bible verses
    * @param translation the given Bible translation (e.g. "esv")
    * @param book        the given Bible book (e.g. "genesis")
    * @param chapter     the given Bible chapter
    * @return the promise of a collection of [[BibleVerse Bible verses]]
    */
  override def apply(translation: String, book: String, chapter: String)(implicit ec: ExecutionContext): Future[List[BibleVerse]] = {
    val startTime = js.Date.now()
    logger.info(s"Retrieving '$translation/$book/$chapter' [${toURL(translation, book, chapter)}]...")
    for {
      (_, html) <- Request.getFuture(toURL(translation, book, chapter))
      profileOpt <- parseHtml(book, chapter, html.toString, startTime)
    } yield profileOpt
  }

  @inline
  private def toURL(translation: String, book: String, chapter: String): String = s"https://biblehub.com/$translation/$book/$chapter.htm"

}

