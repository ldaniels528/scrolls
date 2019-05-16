package com.github.ldaniels528.bible.webapp.client

import com.github.ldaniels528.bible.webapp.client.controllers._
import com.github.ldaniels528.bible.webapp.client.services._
import io.scalajs.npm.angularjs.uirouter.{RouteProvider, RouteTo}
import io.scalajs.npm.angularjs.{Module, angular}

import scala.scalajs.js
import scala.scalajs.js.annotation.JSExport

/**
  * Bible Web Client
  * @author lawrence.daniels@gmail.com
  */
object BibleWebClientJsApp {

  @JSExport
  def main(args: Array[String]): Unit = {
    // create the application
    val module = angular.createModule("bible",
      js.Array("ngAnimate", "ngCookies", "ngRoute", "ngSanitize", "nvd3", "angularFileUpload", "toaster", "ui.bootstrap"))

    // add the controllers and services
    configureControllers(module)
    configureServices(module)

    // define the routes
    module.config({ ($routeProvider: RouteProvider) =>
      // configure the routes
      $routeProvider
        .when("/home", new RouteTo(templateUrl = "/views/home/home.html"))
        .otherwise(new RouteTo(redirectTo = "/home"))
      ()
    })

    // initialize the application
    module.run({ ($rootScope: RootScope) =>
      $rootScope.version = "0.1"
    })
    ()
  }

  private def configureServices(module: Module): Unit = {
    module.serviceOf[BibleService]("BibleService")
    ()
  }

  private def configureControllers(module: Module): Unit = {
    module.controllerOf[HomeController]("HomeController")
    module.controllerOf[MainController]("MainController")
    ()
  }

}
