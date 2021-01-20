import sbt._

object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val bootstrapVersion = "2.3.0"
  private val govukTemplateVersion = "5.60.0-play-26"
  private val playUiVersion = "8.20.0-play-26"
  private val playPartialsVersion = "7.0.0-play-26"
  private val hmrcTestVersion = "3.9.0-play-26"
  private val scalaTestVersion = "3.0.9"
  private val scalaTestPlusVersion = "3.1.3"
  private val pegdownVersion = "1.6.0"
  private val cachingClientVersion = "9.1.0-play-26"
  private val mongoCachingVersion = "6.15.0-play-26"
  private val localTemplateRendererVersion = "2.10.0-play-26"
  private val wireMockVersion = "2.26.3"
  private val reactiveMongoVersion = "7.30.0-play-26"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "http-caching-client" % cachingClientVersion,
    "uk.gov.hmrc" %% "mongo-caching" % mongoCachingVersion,
    "uk.gov.hmrc" %% "local-template-renderer" % localTemplateRendererVersion,
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
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % "3.3.3" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply() = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.13.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "org.mockito" % "mockito-core" % "3.3.3" % scope,
        "com.github.tomakehurst" % "wiremock" % wireMockVersion % scope,
        "com.github.tomakehurst" % "wiremock-jre8" % "2.26.3" % "test,it"
      )
    }.test
  }

  def apply() = compile ++ Test() ++ IntegrationTest()
}
