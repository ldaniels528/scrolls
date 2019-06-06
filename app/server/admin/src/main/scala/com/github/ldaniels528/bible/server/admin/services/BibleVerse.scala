package com.github.ldaniels528.bible.server.admin.services

import scala.scalajs.js

/**
  * Represents a Bible Verse
  * @param book         the Bible book (e.g. "john")
  * @param chapter      the chapter of the book
  * @param verse        the verse of the book
  * @param text         the text of the verse
  * @param responseTime the response time of the service
  */
class BibleVerse(val book: String,
                 val chapter: Int,
                 val verse: Int,
                 val text: String,
                 val responseTime: Double) extends js.Object

/**
  * Bible Verse Companion
  * @author lawrence.daniels@gmail.com
  */
object BibleVerse {

  /**
    * Creates a new Bible Verse
    * @param book         the Bible book (e.g. "john")
    * @param chapter      the chapter of the book
    * @param verse        the verse of the book
    * @param text         the text of the verse
    * @param responseTime the response time of the service
    * @return a new [[BibleVerse]]
    */
  def apply(book: String,
            chapter: Int,
            verse: Int,
            text: String,
            responseTime: Double): BibleVerse = new BibleVerse(book, chapter, verse, text, responseTime)

}
