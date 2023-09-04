import sbt.*

object AppDependencies {

  import play.sbt.PlayImport.*

  private val bootstrapVersion = "7.21.0"
  private val playFrontendVersion = "7.19.0-play-28"
  private val playPartialsVersion = "8.4.0-play-28"
  private val mongoPlayVersion = "1.3.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc" % playFrontendVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-28" % mongoPlayVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply(): Seq[sbt.ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % scope,
        "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % mongoPlayVersion % scope,
        "org.jsoup" % "jsoup" % "1.15.4" % scope,
        "org.scalatestplus" %% "scalatestplus-mockito" % "1.0.0-M2" % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[sbt.ModuleID] = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "uk.gov.hmrc" %% "bootstrap-test-play-28" % bootstrapVersion % scope,
        "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-28" % mongoPlayVersion % scope,
        "org.jsoup" % "jsoup" % "1.15.4" % scope
      )
    }.test
  }


  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
