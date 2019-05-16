package com.github.ldaniels528.bible.webapp.client.services

import com.github.ldaniels528.bible.models.Verse
import io.scalajs.npm.angularjs.Service
import io.scalajs.npm.angularjs.http.{Http, HttpResponse}

import scala.concurrent.ExecutionContext
import scala.scalajs.js

/**
  * Bible Service
  * @param $http the [[Http]] service
  * @author lawrence.daniels@gmail.com
  */
case class BibleService($http: Http) extends Service {

  def getChapter(book: String, chapter: String)(implicit ec: ExecutionContext): js.Promise[HttpResponse[js.Array[Verse]]] = {
    $http.get[js.Array[Verse]](s"/api/$book/$chapter")
  }

  def getVerse(book: String, chapter: String, verse: String)(implicit ec: ExecutionContext): js.Promise[HttpResponse[Verse]] = {
    $http.get[Verse](s"/api/$book/$chapter/$verse")
  }

  def getVerses(book: String, chapter: String, fromVerse: String, toVerse: String)(implicit ec: ExecutionContext): js.Promise[HttpResponse[js.Array[Verse]]] = {
    $http.get[js.Array[Verse]](s"/api/$book/$chapter/$fromVerse/$toVerse")
  }

}
