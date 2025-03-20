import play.sbt.routes.RoutesKeys
import sbt.*
import uk.gov.hmrc.DefaultBuildSettings
import uk.gov.hmrc.DefaultBuildSettings.*
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "income-tax-view-change-dynamic-stub"
val playFrontendHMRCVersion = "8.1.0"
val bootstrapPlayVersion = "8.6.0"
val scalaMockVersion = "5.2.0"
val hmrcMongoPlayVersion = "2.6.0"
val scalaTestVersion = "3.1.1.0"
val pegdownVersion = "1.6.0"
val scalaTestPlusVersion = "7.0.0"
val currentScalaVersion = "2.13.16"
val circeVersion = "0.14.10"

val compile: Seq[ModuleID] = Seq(
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % hmrcMongoPlayVersion, ws,
  "com.github.fge" % "json-schema-validator" % "2.2.6",
  "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendHMRCVersion,
  "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapPlayVersion,
  "io.circe" %% "circe-core" % "0.14.12",
  "io.circe" %% "circe-parser" % "0.14.12",
  "io.circe" %% "circe-yaml" % "1.15.0"
)

def test(scope: String = "test"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapPlayVersion % scope,
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoPlayVersion % scope,
  "org.pegdown" % "pegdown" % pegdownVersion % scope,
  "org.jsoup" % "jsoup" % "1.11.3" % scope,
  "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
  "org.scalamock" %% "scalamock" % scalaMockVersion % scope,
  "org.scalatestplus" %% "mockito-3-2" % scalaTestVersion % scope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % scope,
  "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope,
)

def it(scope: String = "it"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc" %% "bootstrap-backend-play-30" % bootstrapPlayVersion % scope,
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % hmrcMongoPlayVersion % scope,
  "org.pegdown" % "pegdown" % pegdownVersion % scope,
  "org.jsoup" % "jsoup" % "1.11.3" % scope,
  "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
  "org.scalamock" %% "scalamock" % scalaMockVersion % scope,
  "org.scalatestplus" %% "mockito-3-2" % scalaTestVersion % scope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % scope,
  "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapPlayVersion % scope,
)

lazy val appDependencies: Seq[ModuleID] = compile ++ test()
lazy val appItDependencies: Seq[ModuleID] = it()
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;models/.data/..*;" +
      "filters.*;.handlers.*;components.*;.*BuildInfo.*;.*FrontendAuditConnector.*;.*Routes.*;views.html.templates.*;views.html.feedback.*;config.*;" +
      "controllers.feedback.*;app.*;prod.*;config.*;com.*;testOnly.*;\"",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(playSettings: _*)
  .settings(scalaSettings: _*)
  .settings(scalaVersion := currentScalaVersion)
  .settings(scoverageSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(majorVersion := 0)
//  .settings(RoutesKeys.routesImport -= "controllers.Assets.Asset")
//  .settings(scalacOptions += "-Wconf:cat=unused-imports&src=routes/.*:s")
//  .settings(scalacOptions += "-Wconf:cat=lint-multiarg-infix:silent")
//  .settings(scalacOptions += "-Wconf:src=routes/.*:s")
//  .settings(scalacOptions += "-Xfatal-warnings")
  .settings(
    libraryDependencies ++= appDependencies,
    retrieveManaged := true
  )
  .settings(
    libraryDependencies ++= Seq(
      "io.circe" %% "circe-core",
      "io.circe" %% "circe-generic",
      "io.circe" %% "circe-parser"
    ).map(_ % circeVersion)
  )
//  .settings(
//    TwirlKeys.templateImports ++= Seq(
//      "uk.gov.hmrc.govukfrontend.views.html.components._",
//      "uk.gov.hmrc.hmrcfrontend.views.html.components._"
//    )
//  )
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo
  ))

lazy val it = project
  .dependsOn(microservice % "test->test")
  .settings(DefaultBuildSettings.itSettings(false))
  .enablePlugins(play.sbt.PlayScala)
  .settings(
    publish / skip := true
  )
  .settings(scalaVersion := currentScalaVersion)
  .settings(majorVersion := 1)
  .settings(scalacOptions += "-Xfatal-warnings")
  .settings(
    testForkedParallel := true
  )
  .settings(
    libraryDependencies ++= appItDependencies
  )