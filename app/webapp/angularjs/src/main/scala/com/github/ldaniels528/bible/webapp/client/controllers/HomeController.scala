package com.github.ldaniels528.bible.webapp.client.controllers

import com.github.ldaniels528.bible.models.{BibleBook, Verse}
import com.github.ldaniels528.bible.webapp.client.RootScope
import com.github.ldaniels528.bible.webapp.client.controllers.HomeController._
import com.github.ldaniels528.bible.webapp.client.services.BibleService
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.{Controller, Log, injected}

import scala.scalajs.concurrent.JSExecutionContext.Implicits.queue
import scala.scalajs.js
import scala.scalajs.js.JSConverters._
import scala.util.{Failure, Random, Success}

/**
  * Home Controller
  * @author lawrence.daniels@gmail.com
  */
case class HomeController($scope: HomeScope, $log: Log,
                          @injected(name = "BibleService") bibleService: BibleService) extends Controller {

  /**
    * Initializes the controller
    */
  def init(): Unit = {
    console.info(s"Initializing ${getClass.getSimpleName}...")

    $scope.listMode = true

    // get a random book of the bible
    val aRandomBook = bibleBooks(new Random().nextInt(bibleBooks.length)).name

    // select the first chapter of the book
    $scope.selection = new Selection(book = aRandomBook, chapter = "1", verses = js.undefined)

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

    // pre-load the book's chapter
    for {
      book <- $scope.selection.flatMap(_.book)
      chapter <- $scope.selection.flatMap(_.chapter)
    } {
      loadChapter(book, chapter)
    }
  }

  /**
    * Loads the specified chapter for the given book name
    * @param aName    the name of the book to load
    * @param aChapter the chapter of the book to load
    */
  def loadChapter(aName: js.UndefOr[String], aChapter: js.UndefOr[String], aVerse: js.UndefOr[String] = js.undefined): Unit = {
    for {
      name <- aName
      chapter <- aChapter
    } {
      bibleService.getChapter(name, chapter).toFuture onComplete {
        case Success(response) =>
          $scope.$apply(() => $scope.verses = response.data)
        case Failure(e) =>
          console.error(s"Failed to read chapters for $name/$chapter: ${e.getMessage}")
      }
    }
  }

  /**
    * Performs the search for a scripture or set of scriptures
    * @param aSearchText the given search text (e.g. "luke 12:8-11")
    */
  def search(aSearchText: js.UndefOr[String]): Unit = aSearchText foreach { searchText =>
    parseSelection(searchText).toOption match {
      case Some(selection) =>
        $scope.selection = selection
        updateChapters(selection.book, selection.chapter)
      case None =>
        addError(s"Could not decipher '$searchText'")
    }
  }

  def updateChapters(aName: js.UndefOr[String], aChapter: js.UndefOr[String] = "1"): Unit = {
    for {
      name <- aName
      chapter <- aChapter
    } {
      console.info(s"Updating chapters for $name")
      //$scope.selection.foreach(_.chapter = chapter)
      $scope.chapters = bibleBooks.find(_.name == name).map(bb => (1 to bb.chapters).map(_.toString).toJSArray).orUndefined
      loadChapter($scope.selection.flatMap(_.book), $scope.selection.flatMap(_.chapter))
    }
  }

  private def addError(message: String): Unit = {
    console.error(message)
    if ($scope.errors.isEmpty) $scope.errors = new js.Array[String]()
    $scope.errors.foreach(_.push(message))
  }

  /**
    * Parses the given search text and attempts to extract a selection
    * @param searchText the given search text
    * @return the [[Selection]] or <code>undefined</code>
    */
  private def parseSelection(searchText: String): js.UndefOr[Selection] = {
    val terms = searchText.toLowerCase().map {
      case c if c.isLetterOrDigit => c
      case _ => ' '
    }.trim.replaceAllLiterally("  ", " ").split(' ').toList

    def range(verse0: js.UndefOr[String], verse1: js.UndefOr[String]): js.UndefOr[js.Array[String]] = {
      (verse0, verse1) match {
        case (v0, v1) if v0.isEmpty & v1.isEmpty => js.undefined
        case (v0, v1) if v0.isEmpty => v1.map(js.Array(_))
        case (v0, v1) if v1.isEmpty => v0.map(js.Array(_))
        case _ =>
          for {
            v0 <- verse0.map(_.toInt)
            v1 <- verse1.map(_.toInt)
            r0 = Math.min(v0, v1)
            r1 = Math.max(v0, v1)
          } yield (r0 to r1).map(_.toString).toJSArray
      }
    }

    terms match {
      case book :: chapter :: Nil => new Selection(book, chapter, verses = js.undefined)
      case book :: chapter :: verse :: Nil => new Selection(book, chapter, verses = js.Array(verse))
      case book :: chapter :: fromVerse :: toVerse :: Nil => new Selection(book, chapter, verses = range(fromVerse, toVerse))
      case _ => js.undefined
    }
  }

  /////////////////////////////////////////////////////////////////////////////////
  //      Exported Methods
  /////////////////////////////////////////////////////////////////////////////////

  $scope.initHome = () => init()

  $scope.isSelected = (aVerse: js.UndefOr[Verse]) => {
    (for {
      verses <- $scope.selection.flatMap(_.verses)
      verse <- aVerse.map(_.verse)
    } yield verses.contains(verse)).contains(true)
  }

  $scope.loadChapter = (aName: js.UndefOr[String], aChapter: js.UndefOr[String]) => {
    $scope.errors = js.undefined
    loadChapter(aName, aChapter)
  }

  $scope.search = (aSearchText: js.UndefOr[String]) => {
    $scope.errors = js.undefined
    search(aSearchText)
  }

  $scope.updateChapters = (aName: js.UndefOr[String]) => updateChapters(aName)

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

  /**
    * Represents a bible chapter or verse selection
    * @param book    the selected Bible book
    * @param chapter the selected Bible chapter
    * @param verses  the selected Bible verse
    */
  class Selection(val book: js.UndefOr[String],
                  val chapter: js.UndefOr[String],
                  val verses: js.UndefOr[js.Array[String]]) extends js.Object

}

/**
  * Home Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait HomeScope extends RootScope {
  // variables
  var bibleBookNames: js.UndefOr[js.Array[BibleBookJS]] = js.native
  var chapters: js.UndefOr[js.Array[String]] = js.native
  var errors: js.UndefOr[js.Array[String]] = js.native
  var listMode: js.UndefOr[Boolean] = js.native
  var verses: js.UndefOr[js.Array[Verse]] = js.native
  var searchText: js.UndefOr[String] = js.native
  var selection: js.UndefOr[Selection] = js.native

  // functions
  var initHome: js.Function0[Unit] = js.native
  var isSelected: js.Function1[js.UndefOr[Verse], Boolean] = js.native
  var loadChapter: js.Function2[js.UndefOr[String], js.UndefOr[String], Unit] = js.native
  var search: js.Function1[js.UndefOr[String], Unit] = js.native
  var updateChapters: js.Function1[js.UndefOr[String], Unit] = js.native

}