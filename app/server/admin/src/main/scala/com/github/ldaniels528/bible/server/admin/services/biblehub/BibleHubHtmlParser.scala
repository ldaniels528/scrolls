package com.github.ldaniels528.bible.server.admin.services.biblehub

import com.github.ldaniels528.bible.server.admin.services.{BibleVerse, Tag}
import com.github.ldaniels528.bible.webapp.server.util.LoggerFactory
import io.scalajs.nodejs.Error
import io.scalajs.npm.htmlparser2.{Parser, ParserHandler, ParserOptions}
import io.scalajs.util.OptionHelper._

import scala.concurrent.{Future, Promise}
import scala.language.postfixOps
import scala.scalajs.js
import scala.scalajs.runtime.wrapJavaScriptException

/**
  * Bible Hub HTML Parser
  * @author lawrence.daniels@gmail.com
  */
trait BibleHubHtmlParser {
  protected val logger: LoggerFactory.Logger = LoggerFactory.getLogger(getClass)

  /**
    * Parsing the HTML into the option of a Bar Chart company profile
    * @param book      the given symbol (e.g. "AAPL")
    * @param html      the given HTML document
    * @param startTime the given service execution start time
    * @return the option of a [[BibleVerse Bar Chart profile]]
    */
  def parseHtml(book: String, chapter: String, html: String, startTime: Double): Future[List[BibleVerse]] = {
    val promise = Promise[List[BibleVerse]]()
    val tagStack = js.Array[Tag]()
    val mappings = js.Dictionary[List[String]]()
    var lastText_? : Option[String] = None
    var key_? : Option[String] = None
    val parser = new Parser(new ParserHandler {

      override def onopentag(name: String, attributes: js.Dictionary[String]): Unit = {
        tagStack.push(new Tag(name, attributes))
        ()
      }

      override def onclosetag(name: String): Unit = {
        val tag = tagStack.pop()
        //logger.info(s"onclosetag: ${JSON.stringify(tag)} [$tagPath|$name]")
        tagPath match {
          case "html.body.div.table.tr.td.div.table.tr.td.div.div.div.span.p.span.a" |
               "html.body.div.table.tr.td.div.table.tr.td.div.div.div.span.a" |
               "html.body.div.table.tr.td.div.table.tr.td.div.div.div.p.span.a" if name == "b" =>
            key_? = (lastText_? ?? Option(tag.text)).map(_.trim) flatMap {
              case s if s.matches("\\d+") => Option(s)
              case s =>
                logger.warn(s"Non-numeric key '$s'")
                None
            }
            //key_?.foreach(key => logger.info(s"Found new key $key"))
          case "html.body.div.table.tr.td.div.table.tr.td.div" if name == "div" =>
            key_? = None
          case _ =>
        }
      }

      override def ontext(text: String): Unit = {
        val trimmedText = text.trim
        lastText_? = if (trimmedText.nonEmpty) Option(trimmedText) else None
        lastText_? foreach { _ =>
          //logger.info(s"ontext: `$trimmedText` [$tagPath]")

          key_? foreach { key =>
            tagStack.lastOption foreach (_.text += text)
            mappings.get(key) match {
              case Some(list) => mappings(key) = list ::: text :: Nil
              case None => mappings(key) = List(text)
            }
          }
        }
      }

      override def onend() {
        promise.success(mappings.toSeq sortBy (_._1.toInt) map { case (verse, list) =>
          new BibleVerse(
            book = book,
            chapter = chapter,
            verse = verse,
            text = list.init.mkString,
            responseTime = System.currentTimeMillis() - startTime)
        } toList)
      }

      override def onerror(err: Error): Unit = promise.failure(wrapJavaScriptException(err))

      private def tagPath = tagStack.map(_.name).mkString(".")

    }, new ParserOptions(decodeEntities = true, lowerCaseTags = true))

    parser.write(html)
    parser.end()
    promise.future
  }

}
