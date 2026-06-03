import sbt.*

object AppDependencies {

  import play.sbt.PlayImport.*

  private val bootstrapVersion                  = "10.7.0"
  private val playFrontendVersion               = "13.7.0"
  private val mongoPlayVersion                  = "2.12.0"
  private val pekkoVersion                      = "1.6.0"
  private val playConditionalFormMappingVersion = "3.5.0"
  private val jsoupVersion                      = "1.22.2"
  private val scalacheck1_18Version             = "3.2.19.0"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % playFrontendVersion,
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % playConditionalFormMappingVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % mongoPlayVersion,
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"                % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30"               % mongoPlayVersion,
    "org.jsoup"          % "jsoup"                                 % jsoupVersion,
    "org.scalatestplus" %% "scalacheck-1-18"                       % scalacheck1_18Version,
    "org.apache.pekko"  %% "pekko-actor-typed"                     % pekkoVersion,
    // These 3 were pulling in the wrong version as transitive dependencies, so have been manually pinned to the same version as above
    "org.apache.pekko"  %% "pekko-protobuf-v3"                     % pekkoVersion,
    "org.apache.pekko"  %% "pekko-serialization-jackson"           % pekkoVersion,
    "org.apache.pekko"  %% "pekko-stream"                          % pekkoVersion,
  ).map(_ % Test)

  val itDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % mongoPlayVersion,
  ).map(_ % Test)

  def apply(): Seq[ModuleID] = compile ++ testDependencies ++ itDependencies
}
