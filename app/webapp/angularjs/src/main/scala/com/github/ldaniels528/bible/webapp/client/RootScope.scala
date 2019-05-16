package com.github.ldaniels528.bible.webapp.client

import io.scalajs.npm.angularjs.Scope

import scala.scalajs.js

/**
  * Bible Application Root Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait RootScope extends Scope {
  var version: js.UndefOr[String] = js.native

}
