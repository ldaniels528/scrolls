package com.github.ldaniels528.bible.webapp.server.dao

import scala.concurrent.Future

/**
  * Represents a Bible DAO
  * @author lawrence.daniels@gmail.com
  */
trait BibleDAO {

  def find(book: String, chapter: Int): Future[List[Scripture]]

  def find(book: String, chapter: Int, verse: Int): Future[Option[Scripture]]

  def find(book: String, chapter: Int, fromVerse: Int, toVerse: Int): Future[List[Scripture]]

  def findAll: Future[List[Scripture]]

  def where(f: Scripture => Boolean): Future[List[Scripture]]

}

/**
  * Represents a Bible DAO
  * @author lawrence.daniels@gmail.com
  */
object BibleDAO {

  /**
    * The collection of Bible Books
    */
  val bibleBooks: List[BibleBook] = List(
    // old testament
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
    BibleBook(name = "revelation", chapters = 22)
  )

  /**
    * Creates a new DAO instance
    * @return a new [[BibleDAO Bible DAO]]
    */
  def apply(): BibleDAO = BibleDAOLocalFiles

  /**
    * Represents a bible book
    * @param name     the Bible book name
    * @param chapters the number of chapters the book contains
    */
  case class BibleBook(name: String, chapters: Int)

}