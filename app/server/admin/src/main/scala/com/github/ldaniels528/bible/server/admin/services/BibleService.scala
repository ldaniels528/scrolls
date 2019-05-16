package com.github.ldaniels528.bible.server.admin.services

import com.github.ldaniels528.bible.server.admin.services.biblehub.{BibleHubHtmlFileService, BibleHubService}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Bible Service
  * @author lawrence.daniels@gmail.com
  */
trait BibleService {

  /**
    * Returns the promise of a collection of a Bible verses
    * @param translation the given Bible translation (e.g. "esv")
    * @param book        the given Bible book (e.g. "genesis")
    * @param chapter     the given Bible chapter (e.g. "8")
    * @return the promise of a collection of [[BibleVerse Bible verses]]
    */
  def apply(translation: String, book: String, chapter: String)(implicit ec: ExecutionContext): Future[List[BibleVerse]]

  /**
    * Returns the service's provider name
    * @return the service's provider name (e.g. "BibleHub")
    */
  def providerName: String = getClass.getSimpleName.replaceAllLiterally("$", "").replaceAllLiterally("Service", "")

}

/**
  * Bible Service Companion
  * @author lawrence.daniels@gmail.com
  */
object BibleService {

  /**
    * Returns a new Bible service instance
    * @return a new [[BibleService Bible service]] instance
    */
  def apply(): BibleService = BibleHubService

  /**
    * Returns a new Bible service instance
    * @param htmlFile the provider's HTML source file
    * @return a new [[BibleService Bible service]] instance
    */
  def fromFile(htmlFile: String): BibleService = new BibleHubHtmlFileService(htmlFile)

}