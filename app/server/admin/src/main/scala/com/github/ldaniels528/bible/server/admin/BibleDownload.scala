package com.github.ldaniels528.bible.server.admin

import com.github.ldaniels528.bible.server.admin.services.BibleService
import com.github.ldaniels528.bible.webapp.server.dao.{BibleDAO, BibleDAOLocalFiles}
import com.github.ldaniels528.bible.webapp.server.util.LoggerFactory

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.JSExport

/**
  * Bible Download Process
  * @author lawrence.daniels@gmail.com
  */
object BibleDownload {
  private[this] val logger = LoggerFactory.getLogger(getClass)
  private[this] val allBookChapters: List[BookChapter] = BookChapter.getChapters("esv")

  /**
    * Runs the application
    * @param args the command line arguments
    */
  @JSExport
  def main(args: Array[String]): Unit = {
    //implicit val service: BibleService = BibleService.fromFile("./app/server/admin/src/main/resources/bibleHub.html")
    implicit val service: BibleService = BibleService()
    implicit val bibleDAO: BibleDAO = BibleDAOLocalFiles

    //process.argv

    BookChapter.audit(allBookChapters)
    //downloadAndSaveChapters(allBookChapters.filter(r => r.book == "isaiah" && r.chapter == "3"), force = true)
    //downloadAndSaveChapters(allBookChapters)
    //downloadChapterPages(allBookChapters)
  }



}
