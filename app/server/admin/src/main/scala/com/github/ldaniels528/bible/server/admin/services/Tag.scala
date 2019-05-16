package com.github.ldaniels528.bible.server.admin.services

import scala.scalajs.js

/**
  * Represents an HTML tag
  * @param name       the name of the tag (e.g. "table")
  * @param attributes the tag element's attributes
  * @param text       the tag's text
  */
class Tag(val name: String,
          val attributes: js.Dictionary[String],
          var text: String = "") extends js.Object {

  def isClass(className: String): Boolean = attributes.get("class").contains(className)

}