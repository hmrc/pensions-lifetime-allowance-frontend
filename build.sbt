import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, itSettings, scalaSettings}
import sbt.*
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin
import uk.gov.hmrc.versioning.SbtGitVersioning.autoImport.majorVersion

val appName = "pensions-lifetime-allowance-frontend"

lazy val appDependencies: Seq[ModuleID] = Seq.empty
lazy val plugins: Seq[Plugins]          = Seq.empty
lazy val playSettings: Seq[Setting[?]]  = Seq.empty

val scala3_3_7 = "3.3.7"

ThisBuild / majorVersion := 2
ThisBuild / scalaVersion := scala3_3_7

lazy val scoverageSettings = {
  import scoverage.ScoverageKeys
  Seq(
    // Semicolon-separated list of regexs matching classes to exclude
    ScoverageKeys.coverageExcludedPackages := "<empty>;Reverse.*;views.html.*;config.*",
    ScoverageKeys.coverageExcludedFiles    := ".*/Routes.*;.*/RoutesPrefix.*;",
    ScoverageKeys.coverageMinimumStmtTotal := 90,
    ScoverageKeys.coverageFailOnMinimum    := true,
    ScoverageKeys.coverageHighlighting     := true,
    scalacOptions ++= Seq(
      "-Wconf:msg=unused-imports&src=html/.*:s",
      "-Wconf:msg=unused-imports&src=routes/.*:s",
      "-Wconf:msg=unused&src=routes/.*:s",
      "-Wconf:msg=Flag.*repeatedly:s"
    )
  )
}

lazy val root = Project(appName, file("."))
  .enablePlugins((Seq(play.sbt.PlayScala, SbtDistributablesPlugin) ++ plugins) *)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings((playSettings ++ scoverageSettings) *)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    scalaVersion := scala3_3_7,
    libraryDependencies ++= AppDependencies(),
    Test / parallelExecution := false,
    Test / fork              := false,
    retrieveManaged          := true,
    (update / evictionWarningOptions).withRank(KeyRanks.Invisible) := EvictionWarningOptions.default
      .withWarnScalaVersionEviction(false)
  )

PlayKeys.playDefaultPort := 9010

lazy val it = project
  .enablePlugins(PlayScala)
  .dependsOn(root % "test->test")
  .settings(itSettings() *)
