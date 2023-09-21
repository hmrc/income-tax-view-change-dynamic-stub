import play.core.PlayVersion
import play.sbt.routes.RoutesKeys
import sbt._
import uk.gov.hmrc.DefaultBuildSettings._
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin._
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "income-tax-view-change-dynamic-stub"
val playFrontendHMRCVersion = "6.4.0-play-28"
val bootstrapPlayVersion = "7.3.0"
val scalaMockVersion = "5.2.0"
val hmrcMongoPlayVersion = "0.73.0"
val scalaTestVersion = "3.1.1.0"

val compile: Seq[ModuleID] = Seq(
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % hmrcMongoPlayVersion,
  ws,
  "com.github.fge" % "json-schema-validator" % "2.2.6",
  "uk.gov.hmrc" %% "play-frontend-hmrc" % playFrontendHMRCVersion,
  "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapPlayVersion
)

def test(scope: String = "test,it"): Seq[ModuleID] = Seq(
  "uk.gov.hmrc" %% "bootstrap-backend-play-28" % bootstrapPlayVersion % scope,
  "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % "0.73.0"  % scope,
  "org.pegdown" % "pegdown" % "1.6.0" % scope,
  "org.jsoup" % "jsoup" % "1.11.3" % scope,
  "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.1.0" % scope,
  "org.scalamock" %% "scalamock" % scalaMockVersion % scope,
  "org.scalatestplus" %% "mockito-3-2" % scalaTestVersion % scope,
  "com.vladsch.flexmark" % "flexmark-all" % "0.35.10" % scope,
  "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapPlayVersion % scope,
)

lazy val appDependencies: Seq[ModuleID] = compile ++ test()
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
  .settings(scalaVersion := "2.13.8")
  .settings(scoverageSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(majorVersion := 0)
  .settings(RoutesKeys.routesImport -= "controllers.Assets.Asset")
  .settings(
    libraryDependencies ++= appDependencies,
    retrieveManaged := true
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._"
    )
  )
  .settings(
    IntegrationTest / Keys.fork := true,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory) (base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false)
  .settings(resolvers ++= Seq(
    Resolver.jcenterRepo
  ))