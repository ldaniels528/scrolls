import org.scalajs.sbtplugin.ScalaJSPlugin
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import sbt.Keys._
import sbt.Project.projectToRef
import sbt._

val appVersion = "0.2.0"
val appScalaVersion = "2.12.3"
val scalaJsIOVersion = "0.4.2"

/////////////////////////////////////////////////////////////////////////////////
//      Settings
/////////////////////////////////////////////////////////////////////////////////

lazy val jsCommonSettings = Seq(
  javacOptions ++= Seq("-Xlint:deprecation", "-Xlint:unchecked", "-source", "1.8", "-target", "1.8", "-g:vars"),
  scalacOptions ++= Seq("-encoding", "UTF-8", "-target:jvm-1.8", "-unchecked", "-Ywarn-adapted-args", "-Ywarn-value-discard", "-Xlint"),
  scalacOptions ++= Seq("-feature", "-deprecation", "-P:scalajs:sjsDefinedByDefault"),
  scalacOptions in(Compile, doc) ++= Seq("-no-link-warnings"),
  scalaVersion := appScalaVersion,
  autoCompilerPlugins := true,
  relativeSourceMaps := true,
  homepage := Some(url("https://github.com/ldaniels528/shocktrade.js")),
  resolvers += Resolver.sonatypeRepo("releases"),
  libraryDependencies ++= Seq(
    "org.scalacheck" %% "scalacheck" % "1.13.4" % "test",
    "org.scalatest" %%% "scalatest" % "3.0.0" % "test"
  ))

lazy val appSettings = jsCommonSettings ++ Seq(
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  scalaJSUseMainModuleInitializer := true
)

lazy val moduleSettings = jsCommonSettings ++ Seq(
  scalaJSModuleKind := ModuleKind.CommonJSModule,
  scalaJSUseMainModuleInitializer := false
)

lazy val uiSettings = jsCommonSettings ++ Seq(
  scalaJSUseMainModuleInitializer := true
)

/////////////////////////////////////////////////////////////////////////////////
//     Cross-Project Server-side Commons
/////////////////////////////////////////////////////////////////////////////////

lazy val serverCommon = (project in file("./app/server/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "bible-server-common",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mysql" % scalaJsIOVersion
    ))

/////////////////////////////////////////////////////////////////////////////////
//     Web application projects
/////////////////////////////////////////////////////////////////////////////////

lazy val webapp = (project in file("."))
  .aggregate(webClient, webServer)
  .dependsOn(webClient, webServer)
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "bible.js",
    organization := "com.github.ldaniels528",
    version := appVersion,
    scalaVersion := appScalaVersion,
    compile in Compile <<=
      (compile in Compile) dependsOn (fastOptJS in(webClient, Compile)),
    ivyScala := ivyScala.value map (_.copy(overrideScalaVersion = true)),
    Seq(scalaJSUseMainModuleInitializer, fastOptJS, fullOptJS) map { packageJSKey =>
      crossTarget in(webClient, Compile, packageJSKey) := baseDirectory.value / "public" / "javascripts"
    })

lazy val webCommon = (project in file("./app/webapp/common"))
  .enablePlugins(ScalaJSPlugin)
  .settings(moduleSettings: _*)
  .settings(
    name := "bible-web-common",
    organization := "com.github.ldaniels528",
    version := appVersion,
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion
    ))

lazy val webClient = (project in file("./app/webapp/angularjs"))
  .aggregate(webCommon)
  .dependsOn(webCommon)
  .enablePlugins(ScalaJSPlugin)
  .settings(uiSettings: _*)
  .settings(
    name := "bible-web-client-angularjs",
    organization := "com.github.ldaniels528",
    version := appVersion,
    mainClass := Some("com.github.ldaniels528.bible.webapp.client.BibleWebClientJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "dom-html" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "angular-bundle" % scalaJsIOVersion
    ))

lazy val webServer = (project in file("./app/webapp/server"))
  .aggregate(webCommon, serverCommon, webClient)
  .dependsOn(webCommon, serverCommon)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "bible-web-server",
    organization := "com.github.ldaniels528",
    version := appVersion,
    mainClass := Some("com.github.ldaniels528.bible.webapp.server.BibleWebServerJsApp"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-csv" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-fileupload" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "express-ws" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "feedparser-promised" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "md5" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mean-stack" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "mysql" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "splitargs" % scalaJsIOVersion
    ))

/////////////////////////////////////////////////////////////////////////////////
//     Processing application projects
/////////////////////////////////////////////////////////////////////////////////

lazy val admin = (project in file("./app/server/admin"))
  .aggregate(serverCommon)
  .dependsOn(serverCommon)
  .enablePlugins(ScalaJSPlugin)
  .settings(appSettings: _*)
  .settings(
    name := "bible-server-admin",
    organization := "com.github.ldaniels528",
    version := appVersion,
    mainClass := Some("com.github.ldaniels528.bible.server.admin.BibleDownload"),
    libraryDependencies ++= Seq(
      "io.scalajs" %%% "core" % scalaJsIOVersion,
      "io.scalajs" %%% "nodejs" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "csv-parse" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "htmlparser2" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "moment-timezone" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "request" % scalaJsIOVersion,
      "io.scalajs.npm" %%% "xml2js" % scalaJsIOVersion
    ))

/////////////////////////////////////////////////////////////////////////////////
//     Custom Build Functions
/////////////////////////////////////////////////////////////////////////////////

lazy val copyJS = TaskKey[Unit]("copyJS", "Copy JavaScript files to root directory")
copyJS := {
  val out_dir = baseDirectory.value
  val files = for {
    (base, pname, jsname) <- Seq(("webapp", "angularjs", "bible-web-client-angularjs"))
    my_dir = out_dir / "app" / base / pname / "target" / s"scala-${appScalaVersion.take(4)}"
    filePair <- Seq("", ".map").map(s"$jsname-fastopt.js" + _).map(s => (my_dir / s, out_dir / "public" / "javascripts" / s))
  } yield filePair
  IO.copy(files, overwrite = true)
}

// add the alias
addCommandAlias("fastOptJSCopy", ";fastOptJS;copyJS")

// loads the jvm project at sbt startup
onLoad in Global := (Command.process("project webapp", _: State)) compose (onLoad in Global).value
