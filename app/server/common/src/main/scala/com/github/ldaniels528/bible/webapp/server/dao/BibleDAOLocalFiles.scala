package com.github.ldaniels528.bible.webapp.server.dao

import com.github.ldaniels528.bible.webapp.server.util.LoggerFactory
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs

import scala.concurrent.{ExecutionContext, Future}

/**
  * Represents the File-based implementation of Bible DAO
  * @author lawrence.daniels@gmail.com
  */
object BibleDAOLocalFiles extends BibleDAO {
  private val logger = LoggerFactory.getLogger(getClass)
  private val verses = loadBible()

  override def find(book: String, chapter: Int, verse: Int): Future[Option[BibleVerse]] = Future.successful {
    for {
      bookMap <- verses.get(book.toLowerCase)
      chapMap <- bookMap.get(chapter)
      verse <- chapMap.get(verse)
    } yield verse
  }

  override def find(book: String, chapter: Int): Future[List[BibleVerse]] = Future.successful {
    for {
      bookMap <- verses.get(book.toLowerCase).toList
      chapMap <- bookMap.get(chapter).toList
      verses <- chapMap.toList.sortBy(_._1).map(_._2)
    } yield verses
  }

  override def find(book: String, chapter: Int, fromVerse: Int, toVerse: Int)(implicit ec: ExecutionContext): Future[List[BibleVerse]] = {
    find(book, chapter).map(_.filter(r => r.verse.toInt >= fromVerse && r.verse.toInt <= toVerse))
  }

  def loadBible(): Map[String, Map[Int, Map[Int, BibleVerse]]] = {
    val allVerses = for {
      file <- getFiles("scriptures/bible")
      verse <- loadBibleVerses(file)
    } yield verse

    allVerses.groupBy(_.book).map { case (book, bookVerses) =>
      book -> bookVerses.groupBy(_.chapter).map { case (chapter, chapterVerses) =>
        chapter.toInt -> Map(chapterVerses.map(v => v.verse.toInt -> v): _*)
      }
    }
  }

  def loadBibleVerses(file: String): Seq[BibleVerse] = {
    try Fs.readFileSync(file).toString(encoding = "utf8").split("[\n]").map(js => JSON.parseAs[BibleVerse](js)) catch {
      case e: Exception =>
        logger.error(s"Failed to read file '$file': ${e.getMessage}")
        Nil
    }
  }

  def getFiles(path: String): List[String] = {
    Fs.statSync(path) match {
      case stat if stat.isDirectory() =>
        Fs.readdirSync(path).toList.filterNot(_.startsWith(".")).flatMap(subPath => getFiles(s"$path/$subPath"))
      case stat if stat.isFile() => path :: Nil
      case _ => Nil
    }
  }

}