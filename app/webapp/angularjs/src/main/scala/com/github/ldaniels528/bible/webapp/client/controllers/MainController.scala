package com.github.ldaniels528.bible.webapp.client.controllers

import com.github.ldaniels528.bible.webapp.client.RootScope
import io.scalajs.dom.html.browser.console
import io.scalajs.npm.angularjs.{Controller, Log}

import scala.scalajs.js

/**
  * Main Controller
  * @author lawrence.daniels@gmail.com
  */
case class MainController($scope: MainScope, $log: Log) extends Controller {

  $scope.initMain = () => {
    console.info(s"Initializing ${getClass.getSimpleName}...")
  }

}

/**
  * Main Controller Scope
  * @author lawrence.daniels@gmail.com
  */
@js.native
trait MainScope extends RootScope {
  // functions
  var initMain: js.Function0[Unit] = js.native

}