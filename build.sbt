import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, itSettings, scalaSettings}
import sbt.*
import uk.gov.hmrc.*
import DefaultBuildSettings.*
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "pensions-lifetime-allowance-frontend"

lazy val appDependencies: Seq[ModuleID] = Seq.empty
lazy val plugins: Seq[Plugins]          = Seq.empty
lazy val playSettings: Seq[Setting[?]]  = Seq.empty

val scala2_16 = "2.13.16"

ThisBuild / majorVersion := 2
ThisBuild / scalaVersion := scala2_16

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models\\.data\\..*;views.html.*;uk.gov.hmrc.BuildInfo;app.*;prod.*;config.*",
    ScoverageKeys.coverageExcludedFiles    := ".*/Routes.*;.*/RoutesPrefix.*;.*/PdfGeneratorConnector.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum    := false,
    ScoverageKeys.coverageHighlighting     := true,
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    scalacOptions += "-Wconf:cat=unused-imports&src=routes/.*:s"
  )
}

lazy val root = Project(appName, file("."))
  .enablePlugins((Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins) *)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings((playSettings ++ scoverageSettings) *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    scalaVersion := scala2_16,
    libraryDependencies ++= AppDependencies(),
    Test / parallelExecution        := false,
    Test / fork                     := false,
    retrieveManaged                 := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false)
    // Use the silencer plugin to suppress warnings from unused imports in compiled twirl templates
  )
  .settings(
    TwirlKeys.templateImports ++= Seq(
      "uk.gov.hmrc.govukfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.components._",
      "uk.gov.hmrc.hmrcfrontend.views.html.helpers._",
      "uk.gov.hmrc.govukfrontend.views.html.components.implicits._"
    )
  )

PlayKeys.playDefaultPort := 9010

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test")
  .settings(itSettings() *)
