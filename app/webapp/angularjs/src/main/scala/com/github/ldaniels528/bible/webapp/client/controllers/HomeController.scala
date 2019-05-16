package com.github.ldaniels528.bible.webapp.client.controllers

import com.github.ldaniels528.bible.models.Verse
import com.github.ldaniels528.bible.webapp.client.RootScope
import com.github.ldaniels528.bible.webapp.client.controllers.HomeController._
import com.github.ldaniels528.bible.webapp.client.services.BibleService
import io.scalajs.JSON
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.{Controller, Log, injected}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Success}

/**
  * Home Controller
  * @author lawrence.daniels@gmail.com
  */
case class HomeController($scope: HomeScope,
                          $log: Log,
                          @injected(name = "BibleService") bibleService: BibleService) extends Controller {

  $scope.selection = new Selection(book = js.undefined, chapter = js.undefined, verse = js.undefined)

  $scope.bibleBookNames = bibleBooks.map {
    case BibleBook(name, chapters) if name.contains("_") =>
      val displayName = name.split('_').toList match {
        case a :: b :: Nil => s"$a ${b.capitalize}"
        case list => list.mkString(" ")
      }
      new BibleBookJS(name = name, displayName = displayName, chapters = chapters)
    case BibleBook(name, chapters) =>
      new BibleBookJS(name = name, displayName = name.capitalize, chapters = chapters)
  }

  $scope.initHome = () => {
    console.info(s"Initializing ${getClass.getSimpleName}...")
  }

  $scope.loadChapter = (aName: js.UndefOr[String], aChapter: js.UndefOr[String]) => {
    console.info(s"loadChapter($aName, $aChapter)")
    for {
      name <- aName
      chapter <- aChapter
    } {
      bibleService.getChapter(name, chapter).toFuture onComplete {
        case Success(response) =>
          $scope.$apply(() => $scope.verses = response.data)
          console.info(s"verses = ${JSON.stringify(response.data)}")
        case Failure(e) =>
          console.error(s"Failed to read chapters for $name/$chapter: ${e.getMessage}")
      }
    }
  }

  $scope.updateChapters = (aName: js.UndefOr[String]) => aName foreach { name =>
    console.info(s"Updating chapters for $name")
    $scope.selection.foreach(_.chapter = "1")
    $scope.chapters = bibleBooks.find(_.name == name).map(bb => (1 to bb.chapters).map(_.toString).toJSArray).orUndefined
    $scope.loadChapter($scope.selection.flatMap(_.book), $scope.selection.flatMap(_.chapter))
  }

}

/**
  * Home Controller
  * @author lawrence.daniels@gmail.com
  */
object HomeController {
  val bibleBooks = js.Array(
    BibleBook(name = "genesis", chapters = 50),
    BibleBook(name = "exodus", chapters = 40),
    BibleBook(name = "leviticus", chapters = 27),
    BibleBook(name = "numbers", chapters = 36),
    BibleBook(name = "deuteronomy", chapters = 34),
    BibleBook(name = "joshua", chapters = 24),
    BibleBook(name = "judges", chapters = 21),
    BibleBook(name = "ruth", chapters = 4),
    BibleBook(name = "1_samuel", chapters = 31),
    BibleBook(name = "2_samuel", chapters = 24),
    BibleBook(name = "1_kings", chapters = 22),
    BibleBook(name = "2_kings", chapters = 25),
    BibleBook(name = "1_chronicles", chapters = 29),
    BibleBook(name = "2_chronicles", chapters = 36),
    BibleBook(name = "ezra", chapters = 10),
    BibleBook(name = "nehemiah", chapters = 13),
    BibleBook(name = "esther", chapters = 10),
    BibleBook(name = "job", chapters = 42),
    BibleBook(name = "psalms", chapters = 150),
    BibleBook(name = "proverbs", chapters = 31),
    BibleBook(name = "ecclesiastes", chapters = 12),
    BibleBook(name = "songs", chapters = 8),
    BibleBook(name = "isaiah", chapters = 66),
    BibleBook(name = "jeremiah", chapters = 52),
    BibleBook(name = "lamentations", chapters = 5),
    BibleBook(name = "ezekiel", chapters = 48),
    BibleBook(name = "daniel", chapters = 12),
    BibleBook(name = "hosea", chapters = 14),
    BibleBook(name = "joel", chapters = 3),
    BibleBook(name = "amos", chapters = 9),
    BibleBook(name = "obadiah", chapters = 1),
    BibleBook(name = "jonah", chapters = 4),
    BibleBook(name = "micah", chapters = 7),
    BibleBook(name = "nahum", chapters = 3),
    BibleBook(name = "habakkuk", chapters = 3),
    BibleBook(name = "zephaniah", chapters = 3),
    BibleBook(name = "haggai", chapters = 2),
    BibleBook(name = "zechariah", chapters = 14),
    BibleBook(name = "malachi", chapters = 4),

    // new testament
    BibleBook(name = "matthew", chapters = 28),
    BibleBook(name = "mark", chapters = 16),
    BibleBook(name = "luke", chapters = 24),
    BibleBook(name = "john", chapters = 21),
    BibleBook(name = "acts", chapters = 28),
    BibleBook(name = "romans", chapters = 16),
    BibleBook(name = "1_corinthians", chapters = 16),
    BibleBook(name = "2_corinthians", chapters = 13),
    BibleBook(name = "galatians", chapters = 6),
    BibleBook(name = "ephesians", chapters = 6),
    BibleBook(name = "philippians", chapters = 4),
    BibleBook(name = "colossians", chapters = 4),
    BibleBook(name = "1_thessalonians", chapters = 5),
    BibleBook(name = "2_thessalonians", chapters = 3),
    BibleBook(name = "1_timothy", chapters = 6),
    BibleBook(name = "2_timothy", chapters = 4),
    BibleBook(name = "titus", chapters = 3),
    BibleBook(name = "philemon", chapters = 1),
    BibleBook(name = "hebrews", chapters = 13),
    BibleBook(name = "james", chapters = 5),
    BibleBook(name = "1_peter", chapters = 5),
    BibleBook(name = "2_peter", chapters = 3),
    BibleBook(name = "1_john", chapters = 5),
    BibleBook(name = "2_john", chapters = 1),
    BibleBook(name = "3_john", chapters = 1),
    BibleBook(name = "jude", chapters = 1),
    BibleBook(name = "revelation", chapters = 22))

  class BibleBookJS(val name: String, val displayName: String, val chapters: Int) extends js.Object

  class BibleBook(val name: String, val chapters: Int) extends js.Object

  object BibleBook {
    def apply(name: String, chapters: Int): BibleBook = new BibleBook(name, chapters)

    def unapply(bibleBook: BibleBook): Option[(String, Int)] = Some((bibleBook.name, bibleBook.chapters))
  }

  class Selection(var book: js.UndefOr[String],
                  var chapter: js.UndefOr[String],
                  var verse: js.UndefOr[String]) extends js.Object

}

/**
  * Home Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait HomeScope extends RootScope {
  // variables
  var bibleBookNames: js.UndefOr[js.Array[BibleBookJS]] = js.native
  var selection: js.UndefOr[Selection] = js.native
  var chapters: js.UndefOr[js.Array[String]] = js.native
  var verses: js.UndefOr[js.Array[Verse]] = js.native

  // functions
  var initHome: js.Function0[Unit] = js.native
  var loadChapter: js.Function2[js.UndefOr[String], js.UndefOr[String], Unit] = js.native
  var updateChapters: js.Function1[js.UndefOr[String], Unit] = js.native

}