import sbt._

object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val bootstrapVersion = "5.24.0"
  private val govukTemplateVersion = "5.68.0-play-28"
  private val playUiVersion = "9.6.0-play-28"
  private val playPartialsVersion = "8.3.0-play-28"
  private val scalaTestVersion = "3.0.9"
  private val scalaTestPlusVersion = "5.1.0"
  private val pegdownVersion = "1.6.0"
  private val cachingClientVersion = "9.5.0-play-28"
  private val mongoCachingVersion = "7.0.0-play-28"
  private val localTemplateRendererVersion = "2.15.0-play-28"
  private val wireMockVersion = "2.26.3"
  private val reactiveMongoVersion = "8.0.0-play-28"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "http-caching-client" % cachingClientVersion,
    "uk.gov.hmrc" %% "mongo-caching" % mongoCachingVersion,
    "uk.gov.hmrc" %% "local-template-renderer" % localTemplateRendererVersion excludeAll(ExclusionRule(organization="org.scalactic")),
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "simple-reactivemongo" % reactiveMongoVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.14.3" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "com.vladsch.flexmark"    %   "flexmark-all"              % "0.35.10",
        "org.scalatestplus"       %%  "scalatestplus-mockito"     % "1.0.0-M2" % scope,
        "org.scalatestplus.play"  %%  "scalatestplus-play"        % "5.1.0" % scope,
        "org.scalatestplus"       %%  "scalatestplus-scalacheck"  % "3.1.0.0-RC2" % scope,
        "org.mockito" % "mockito-core" % "3.3.3" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "org.mockito" % "mockito-core" % "3.3.3" % scope,
        "com.github.tomakehurst" % "wiremock" % wireMockVersion % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % "2.26.3" % scope
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
