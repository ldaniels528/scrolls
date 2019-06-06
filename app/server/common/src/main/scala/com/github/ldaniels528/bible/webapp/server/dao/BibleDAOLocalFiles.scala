package com.github.ldaniels528.bible.webapp.server.dao

import com.github.ldaniels528.bible.webapp.server.util.LoggerFactory
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs

import scala.concurrent.Future

/**
  * Represents the File-based implementation of Bible DAO
  * @author lawrence.daniels@gmail.com
  */
object BibleDAOLocalFiles extends BibleDAO {
  private val logger = LoggerFactory.getLogger(getClass)
  private val scriptures = loadScriptures()

  override def find(book: String, chapter: Int, verse: Int): Future[Option[Scripture]] = Future.successful {
    scriptures.find(s => s.book == book & s.chapter == chapter & s.verse == verse)
  }

  override def find(book: String, chapter: Int): Future[List[Scripture]] = Future.successful {
    scriptures.filter(s => s.book == book & s.chapter == chapter).sortBy(_.verse)
  }

  override def find(book: String, chapter: Int, fromVerse: Int, toVerse: Int): Future[List[Scripture]] = Future.successful {
    scriptures.filter(s => s.book == book & s.chapter == chapter & s.verse >= fromVerse & s.verse <= toVerse).sortBy(_.verse)
  }

  override def findAll: Future[List[Scripture]] = Future.successful(scriptures)

  override def where(f: Scripture => Boolean): Future[List[Scripture]] = Future.successful {
    scriptures.filter(f).sortBy(_.verse)
  }

  private def loadScriptureFile(file: String): Seq[Scripture] = {
    try Fs.readFileSync(file).toString(encoding = "utf8").split("[\n]").map(js => JSON.parseAs[Scripture](js)) catch {
      case e: Exception =>
        logger.error(s"Failed to read file '$file': ${e.getMessage}")
        Nil
    }
  }

  private def loadScriptures(): List[Scripture] = {
    val translations = List("esv")
    for {
      translation <- translations
      file <- getFiles(s"scriptures/bible/$translation")
      item <- loadScriptureFile(file)
    } yield Scripture(book = item.book, translation, chapter = item.chapter, verse = item.verse, text = item.text)
  }

  private def getFiles(path: String): List[String] = Fs.statSync(path) match {
    case stat if stat.isDirectory() =>
      Fs.readdirSync(path).toList.filterNot(_.startsWith(".")).flatMap(subPath => getFiles(s"$path/$subPath"))
    case stat if stat.isFile() => path :: Nil
    case _ => Nil
  }

}