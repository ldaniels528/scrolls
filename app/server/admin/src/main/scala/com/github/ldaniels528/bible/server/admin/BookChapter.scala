package com.github.ldaniels528.bible.server.admin

import com.github.ldaniels528.bible.server.admin.BookChapter._
import com.github.ldaniels528.bible.server.admin.services.{BibleService, BibleVerse}
import com.github.ldaniels528.bible.webapp.server.dao.BibleDAO
import com.github.ldaniels528.bible.webapp.server.util.LoggerFactory
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs

import scala.concurrent.ExecutionContext
import scala.scalajs.js
import scala.util.{Failure, Success, Try}

/**
  * Represents a reference to a Bible chapter
  * @param translation the translation code (e.g. "esv")
  * @param book        the name of the Bible book
  * @param chapter     the book chapter
  */
case class BookChapter(translation: String, book: String, chapter: String) {
  private val logger = LoggerFactory.getLogger(getClass)

  val jsonDirectory: String = s"scriptures/bible/$translation/$book".replaceAllLiterally(" ", "_")

  val jsonFileName: String = s"$jsonDirectory/$book-$chapter.json".replaceAllLiterally(" ", "_")

  val htmlDirectory: String = s"scriptures/html/bible/$translation/$book".replaceAllLiterally(" ", "_")

  val htmlFileName: String = s"$htmlDirectory/$book-$chapter.html".replaceAllLiterally(" ", "_")

  def isJsonEmpty: Boolean = {
    ensureDirectory(jsonDirectory)
    !Fs.existsSync(jsonFileName) || Fs.statSync(jsonFileName).size == 0
  }

  def isHtmlEmpty: Boolean = {
    ensureDirectory(htmlDirectory)
    !Fs.existsSync(htmlFileName) || Fs.statSync(htmlFileName).size == 0
  }

  def save(verses: Seq[BibleVerse]): Try[Long] = Try {
    val startTime = System.currentTimeMillis()
    logger.info(s"Writing ${verses.size} verses to '$jsonFileName'...")
    Fs.writeFileSync(jsonFileName, data = verses.map(JSON.stringify(_)).mkString("\n"))
    System.currentTimeMillis() - startTime
  }

}

/**
  * Book Chapter Companion
  * @author lawrence.daniels@gmail.com
  */
object BookChapter {
  private[this] val logger = LoggerFactory.getLogger(getClass)

  def audit(bookChapters: Seq[BookChapter])(implicit ec: ExecutionContext, bibleDAO: BibleDAO): Unit = {
    logger.info("Starting the Audit process...")

    // audit the data
    for {
      ref <- bookChapters
      verses <- bibleDAO.find(book = ref.book, chapter = ref.chapter.toInt)
      verseNumbers = verses.map(_.verse.toInt).sorted
      expectedNumbers = verseNumbers.indices.map(_ + 1)
      commonNumbers = verseNumbers intersect expectedNumbers
      missingVerses = expectedNumbers.filterNot(commonNumbers.contains) if missingVerses.nonEmpty
    } {
      logger.info(s"${ref.book.capitalize}: chapter ${ref.chapter} is missing verses: ${missingVerses.mkString(", ")}")
    }
  }

  def downloadHtmlPages(bookChapters: Seq[BookChapter], force: Boolean = false)
                       (implicit ec: ExecutionContext, service: BibleService): Unit = {
    logger.info(s"Starting the Bible Download process for ${bookChapters.length} chapters...")
    val startTime = js.Date.now()
    lazy val elapsedTime = js.Date.now() - startTime
    for {
      ref <- bookChapters if force | ref.isHtmlEmpty
      page <- service.download(ref.translation, ref.book, ref.chapter) if page.nonEmpty
    } {
      Fs.writeFileSync(ref.htmlFileName, page)
      logger.info(s"Successfully saved ${page.length} bytes to '${ref.htmlFileName}' in $elapsedTime msec.")
    }
  }

  def downloadAndSave(bookChapters: Seq[BookChapter], force: Boolean = false)
                     (implicit ec: ExecutionContext, service: BibleService): Unit = {
    logger.info(s"Starting the Bible Download process for ${bookChapters.length} chapters...")
    for {
      ref <- bookChapters if force | ref.isJsonEmpty
      verses <- service(ref.translation, ref.book, ref.chapter) if verses.nonEmpty
    } {
      ref.save(verses) match {
        case Success(elapsedTime) =>
          logger.info(s"Successfully saved ${verses.size} verses to '${ref.jsonFileName}' in $elapsedTime msec.")
        case Failure(e) =>
          logger.error(s"Failed to save ${verses.size} verses to '${ref.jsonFileName}': ${e.getMessage}")
      }
    }
  }

  def getChapters(translation: String*): List[BookChapter] = for {
    translation <- translation.toList
    bibleBook <- BibleDAO.bibleBooks
    ch <- 1 to bibleBook.chapters
  } yield BookChapter(translation, book = bibleBook.name, chapter = ch.toString)

  private def ensureDirectory(directory: String): String = {
    directory.split('/').foldLeft("") { (path, subDir) =>
      val newPath = if (path.isEmpty) subDir else path + "/" + subDir
      if (!Fs.existsSync(newPath)) {
        logger.info(s"Creating directory '$newPath'...")
        Fs.mkdirSync(newPath)
      }
      newPath
    }
  }

}