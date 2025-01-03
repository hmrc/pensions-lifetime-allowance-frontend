import sbt.*

object AppDependencies {

  import play.sbt.PlayImport.*

  private val bootstrapVersion = "9.6.0"
  private val playFrontendVersion = "11.8.0"
  private val mongoPlayVersion = "2.3.0"
  private val pekkoVersion = "1.0.2"
  private val playConditionalFormMappingVersion = "3.2.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendVersion,
    "uk.gov.hmrc" %% "play-conditional-form-mapping-play-30" % playConditionalFormMappingVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30" % mongoPlayVersion,
    "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion
  )

  val testDependencies: Seq[ModuleID] = Seq(
      "uk.gov.hmrc"        %% "bootstrap-test-play-30"  % bootstrapVersion,
      "uk.gov.hmrc.mongo"  %% "hmrc-mongo-test-play-30" % mongoPlayVersion,
      "org.jsoup"          % "jsoup"                    % "1.17.2",
      "org.scalatestplus"  %% "scalacheck-1-17"         % "3.2.18.0"
    ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = Seq(
        "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrapVersion,
        "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % mongoPlayVersion,
        "org.jsoup" % "jsoup" % "1.17.2"
      ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ testDependencies ++ itDependencies
}
