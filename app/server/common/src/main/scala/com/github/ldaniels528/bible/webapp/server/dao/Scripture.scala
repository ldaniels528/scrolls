package com.github.ldaniels528.bible.webapp.server.dao

import scala.scalajs.js

/**
  * Represents a Scripture
  * @param book        the Bible book (e.g. "john")
  * @param translation the Bible/book translation/version
  * @param chapter     the chapter of the book
  * @param verse       the verse of the book
  * @param text        the text of the verse
  */
class Scripture(val book: String,
                val translation: String,
                val chapter: Int,
                val verse: Int,
                val text: String) extends js.Object


/**
  * Scripture Companion
  * @author lawrence.daniels@gmail.com
  */
object Scripture {

  /**
    * Creates a new scripture
    * @param book        the Bible book (e.g. "john")
    * @param translation the Bible/book translation/version
    * @param chapter     the chapter of the book
    * @param verse       the verse of the book
    * @param text        the text of the verse
    * @return a new [[Scripture scripture]]
    */
  def apply(book: String,
            translation: String,
            chapter: Int,
            verse: Int,
            text: String): Scripture = new Scripture(book, translation, chapter, verse, text)

  /**
    * Scripture Enrichment
    * @param scripture the given [[Scripture scripture]]
    */
  final implicit class ScriptureEnrichment(val scripture: Scripture) extends AnyVal {

    def copy(book: js.UndefOr[String] = js.undefined,
             translation: js.UndefOr[String] = js.undefined,
             chapter: js.UndefOr[Int] = js.undefined,
             verse: js.UndefOr[Int] = js.undefined,
             text: js.UndefOr[String] = js.undefined): Scripture = {
      Scripture(
        book = book.getOrElse(scripture.book),
        translation = translation.getOrElse(scripture.translation),
        chapter = chapter.getOrElse(scripture.chapter),
        verse = verse.getOrElse(scripture.verse),
        text = text.getOrElse(scripture.text)
      )
    }

  }

}