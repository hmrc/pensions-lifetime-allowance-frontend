import sbt.*

object AppDependencies {

  import play.sbt.PlayImport.*

  private val bootstrapVersion                  = "10.7.0"
  private val playFrontendVersion               = "12.32.1"
  private val mongoPlayVersion                  = "2.12.0"
  private val pekkoVersion                      = "1.5.0"
  private val playConditionalFormMappingVersion = "3.5.0"
  private val jsoupVersion                      = "1.22.2"

  val compile: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc"       %% "bootstrap-frontend-play-30"            % bootstrapVersion,
    "uk.gov.hmrc"       %% "play-frontend-hmrc-play-30"            % playFrontendVersion,
    "uk.gov.hmrc"       %% "play-conditional-form-mapping-play-30" % playConditionalFormMappingVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-play-30"                    % mongoPlayVersion,
  )

  val testDependencies: Seq[ModuleID] = Seq(
    "uk.gov.hmrc"       %% "bootstrap-test-play-30"  % bootstrapVersion,
    "uk.gov.hmrc.mongo" %% "hmrc-mongo-test-play-30" % mongoPlayVersion,
    "org.jsoup"          % "jsoup"                   % jsoupVersion,
    "org.scalatestplus" %% "scalacheck-1-17"         % "3.2.18.0",
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
