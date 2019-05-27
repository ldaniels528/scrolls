package com.github.ldaniels528.bible.models

import scala.scalajs.js

/**
  * Represents a Bible Book
  * @param name     the name of the book
  * @param chapters the total number of chapters contained within the book
  */
class BibleBook(val name: String, val chapters: Int) extends js.Object

/**
  * Bible Book Companion
  * @author lawrence.daniels@gmail.com
  */
object BibleBook {

  def apply(name: String, chapters: Int): BibleBook = new BibleBook(name, chapters)

  def unapply(bibleBook: BibleBook): Option[(String, Int)] = Some((bibleBook.name, bibleBook.chapters))

}
