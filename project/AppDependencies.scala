import sbt.*

object AppDependencies {

  import play.sbt.PlayImport.*

  private val bootstrapVersion = "8.4.0"
  private val playFrontendVersion = "8.4.0"
  private val playPartialsVersion = "9.1.0"
  private val mongoPlayVersion = "1.7.0"
  private val pekkoVersion = "1.0.2"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendVersion,
    "uk.gov.hmrc" %% "play-partials-play-30" % playPartialsVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % mongoPlayVersion,
    "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply(): Seq[sbt.ModuleID] = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc"        %% "bootstrap-test-play-30"  % bootstrapVersion % scope,
        "uk.gov.hmrc.mongo"  %% "hmrc-mongo-test-play-30" % mongoPlayVersion % scope,
        "org.jsoup"          % "jsoup"                    % "1.15.4" % scope,
        "org.scalatestplus"  %% "scalacheck-1-17"         % "3.2.16.0"
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
