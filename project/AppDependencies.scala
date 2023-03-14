import sbt._

object AppDependencies {

  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val bootstrapVersion = "7.14.0"
  private val playFrontendVersion = "0.94.0-play-28"
  private val playPartialsVersion = "8.4.0-play-28"
  private val scalaTestPlusVersion = "5.1.0"
  private val pegdownVersion = "1.6.0"
  private val cachingClientVersion = "10.0.0-play-28"
  private val localTemplateRendererVersion = "2.17.0-play-28"
  private val timeVersion = "3.25.0"
  private val mockitoCoreVersion = "4.1.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-28" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-frontend-hmrc"         % playFrontendVersion,
    "uk.gov.hmrc" %% "play-partials"              % playPartialsVersion,
    "uk.gov.hmrc" %% "http-caching-client"        % cachingClientVersion,
    "uk.gov.hmrc" %% "local-template-renderer"    % localTemplateRendererVersion excludeAll(ExclusionRule(organization="org.scalactic")),
    "uk.gov.hmrc" %% "time" % timeVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test: Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply(): Seq[sbt.ModuleID] = new TestDependencies {
      override lazy val test = Seq(

        "uk.gov.hmrc"              %%   "bootstrap-test-play-28"    % bootstrapVersion     % scope,
        "org.scalatest"            %%   "scalatest"                 % "3.2.9"              % scope,
        "org.scalatestplus.play"   %%   "scalatestplus-play"        % scalaTestPlusVersion % scope,
        "org.pegdown"               %   "pegdown"                   % pegdownVersion       % scope,
        "org.jsoup"                 %   "jsoup"                     % "1.15.4"             % scope,
        "com.typesafe.play"        %%   "play-test"                 % PlayVersion.current  % scope,
        "com.vladsch.flexmark"      %   "flexmark-all"              % "0.35.10"            % scope,
        "org.scalatestplus"        %%   "scalatestplus-mockito"     % "1.0.0-M2"           % scope,
        "org.scalatestplus.play"   %%   "scalatestplus-play"        % "5.1.0"              % scope,
        "org.scalatestplus"        %%   "scalatestplus-scalacheck"  % "3.1.0.0-RC2"        % scope,
        "org.mockito"               %   "mockito-core"              % mockitoCoreVersion   % scope
      )
    }.test
  }

  object IntegrationTest {
    def apply(): Seq[sbt.ModuleID] = new TestDependencies {

      override lazy val scope: String = "it"

      override lazy val test = Seq(
        "org.pegdown"             % "pegdown"            % pegdownVersion       % scope,
        "org.jsoup"               % "jsoup"              % "1.15.4"             % scope,
        "com.typesafe.play"      %% "play-test"          % PlayVersion.current  % scope,
        "com.vladsch.flexmark"    % "flexmark-all"       % "0.35.10"            % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "org.mockito"             % "mockito-core"       % mockitoCoreVersion   % scope,
        "com.github.tomakehurst"  % "wiremock"           % "2.27.2"             % scope,
        "com.github.tomakehurst"  % "wiremock-jre8"      % "2.31.0"             % scope
      )
    }.test
  }

  def apply(): Seq[ModuleID] = compile ++ Test() ++ IntegrationTest()
}
