package com.github.ldaniels528.bible.server.admin.services.biblehub

import com.github.ldaniels528.bible.server.admin.services.{BibleService, BibleVerse}
import io.scalajs.nodejs.fs.Fs

import scala.concurrent.{ExecutionContext, Future}
import scala.scalajs.js

/**
  * Bible Hub Service (local HTML file version)
  * @param htmlFile the provider's HTML source file
  * @author lawrence.daniels@gmail.com
  */
class BibleHubHtmlFileService(htmlFile: String) extends BibleService with BibleHubHtmlParser {

  /**
    * Returns the promise of a collection of a Bible verses
    * @param translation the given Bible translation (e.g. "esv")
    * @param book        the given Bible book (e.g. "genesis")
    * @param chapter     the given Bible chapter (e.g. "8")
    * @return the promise of a collection of [[BibleVerse Bible verses]]
    */
  override def apply(translation: String, book: String, chapter: String)(implicit ec: ExecutionContext): Future[List[BibleVerse]] = {
    val startTime = js.Date.now()
    for {
      html <- Fs.readFileFuture(htmlFile).map(_.toString)
      profileOpt <- parseHtml(book, chapter, html.toString, startTime)
    } yield profileOpt
  }

  /**
    * Returns the promise of a collection of a Bible verses
    * @param translation the given Bible translation (e.g. "esv")
    * @param book        the given Bible book (e.g. "genesis")
    * @param chapter     the given Bible chapter
    * @return the promise of a collection of [[BibleVerse Bible verses]]
    */
  override def download(translation: String, book: String, chapter: String)(implicit ec: ExecutionContext): Future[String] = {
    Fs.readFileFuture(htmlFile).map(_.toString)
  }

}
