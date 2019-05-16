package com.github.ldaniels528.bible.webapp.server.dao

import scala.scalajs.js

/**
  * Represents a Bible Verse
  * @param book    the Bible book (e.g. "john")
  * @param chapter the chapter of the book
  * @param verse   the verse of the book
  * @param text    the text of the verse
  */
class BibleVerse(val book: String,
                 val chapter: String,
                 val verse: String,
                 val text: String) extends js.Object