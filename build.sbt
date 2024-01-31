import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings, targetJvm}
import com.typesafe.sbt.web.Import.pipelineStages
import com.typesafe.sbt.web.Import.Assets
import sbt.Keys._
import sbt._
import uk.gov.hmrc._
import DefaultBuildSettings._
import uk.gov.hmrc.SbtAutoBuildPlugin
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "pensions-lifetime-allowance-frontend"

lazy val appDependencies: Seq[ModuleID] = Seq.empty
lazy val plugins: Seq[Plugins] = Seq.empty
lazy val playSettings: Seq[Setting[_]] = Seq.empty

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;.*AuthService.*;models\\.data\\..*;views.html.*;uk.gov.hmrc.BuildInfo;app.*;prod.*;config.*",
    ScoverageKeys.coverageExcludedFiles := ".*/Routes.*;.*/RoutesPrefix.*;.*/PdfGeneratorConnector.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum := false,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val root = Project(appName, file("."))
  .enablePlugins(Seq(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin) ++ plugins: _*)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(playSettings ++ scoverageSettings: _*)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scalaVersion := "2.13.12",
    libraryDependencies ++= AppDependencies(),
    Test / parallelExecution := false,
    Test / fork := false,
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
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
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    IntegrationTest / Keys.fork := false,
    IntegrationTest / unmanagedSourceDirectories := (IntegrationTest / baseDirectory) (base => Seq(base / "it")).value,
    addTestReportOption(IntegrationTest, "int-test-reports"),
    IntegrationTest / parallelExecution := false)
  .settings(majorVersion := 2)
  PlayKeys.playDefaultPort := 9010