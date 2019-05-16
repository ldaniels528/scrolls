package com.github.ldaniels528.bible.server.admin

import com.github.ldaniels528.bible.server.admin.services.{BibleService, BibleVerse}
import com.github.ldaniels528.bible.server.dao.BibleDAOLocalFiles
import com.github.ldaniels528.bible.webapp.server.dao.{BibleDAO, BibleDAOLocalFiles}
import com.github.ldaniels528.bible.webapp.server.util.LoggerFactory
import io.scalajs.JSON
import io.scalajs.nodejs.fs.Fs

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js.annotation.JSExport
import scala.util.{Failure, Success, Try}

/**
  * Bible Download Process
  * @author lawrence.daniels@gmail.com
  */
object BibleDownload {
  private[this] val logger = LoggerFactory.getLogger(getClass)

  private val allBookChapters: List[ChapterRef] = for {
    translation <- List("esv")
    bibleBook <- BibleDAO.bibleBooks
    ch <- 1 to bibleBook.chapters
  } yield ChapterRef(translation, book = bibleBook.name, chapter = ch.toString)

  @JSExport
  def main(args: Array[String]): Unit = {
    //implicit val service: BibleService = BibleService.fromFile("./app/server/admin/src/main/resources/bibleHub.html")
    implicit val service: BibleService = BibleService()
    implicit val bibleDAO: BibleDAO = BibleDAOLocalFiles

    //auditScriptures()
    //downloadAndSaveChapters(allBookChapters.filter(r => r.book == "isaiah" && r.chapter == "3"), force = true)
    downloadAndSaveChapters()
  }

  def auditScriptures(bookChapters: Seq[ChapterRef] = allBookChapters)(implicit bibleDAO: BibleDAO): Unit = {
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

  def downloadAndSaveChapters(bookChapters: Seq[ChapterRef] = allBookChapters, force: Boolean = false)
                             (implicit service: BibleService): Unit = {
    logger.info(s"Starting the Bible Download process for ${bookChapters.length} chapters...")
    for {
      ref <- bookChapters if force | ref.isEmpty
      verses <- service(ref.translation, ref.book, ref.chapter) if verses.nonEmpty
      result = saveChapter(ref, verses)
    } {
      result match {
        case Success(elapsedTime) =>
          logger.info(s"Successfully saved ${verses.size} verses to '${ref.fileName}' in $elapsedTime msec.")
        case Failure(e) =>
          logger.error(s"Failed to save ${verses.size} verses to '${ref.fileName}': ${e.getMessage}")
      }
    }
  }

  private def saveChapter(ref: ChapterRef, verses: Seq[BibleVerse]): Try[Long] = Try {
    val startTime = System.currentTimeMillis()
    logger.info(s"Writing ${verses.size} verses to '${ref.fileName}'...")
    Fs.writeFileSync(ref.fileName, data = verses.map(JSON.stringify(_)).mkString("\n"))
    System.currentTimeMillis() - startTime
  }

  case class ChapterRef(translation: String, book: String, chapter: String) {

    val directory: String = s"scriptures/bible/$translation/$book".replaceAllLiterally(" ", "_")

    val fileName: String = s"$directory/$book-$chapter.json".replaceAllLiterally(" ", "_")

    def isEmpty: Boolean = {
      ensureDirectory(directory)
      !Fs.existsSync(fileName) || Fs.statSync(fileName).size == 0
    }

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

}
