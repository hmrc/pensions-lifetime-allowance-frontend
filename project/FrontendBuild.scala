/*
 * Copyright 2016 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import sbt._

object FrontendBuild extends Build with MicroService {

  val appName = "pensions-lifetime-allowance-frontend"

  override lazy val appDependencies: Seq[ModuleID] = AppDependencies()
}

private object AppDependencies {
  import play.sbt.PlayImport._
  import play.core.PlayVersion

  private val playHealthVersion = "2.2.0"
  private val loggerVersion = "3.1.0"
  private val frontendBootstrapVersion = "8.24.0"
  private val govukTemplateVersion = "5.10.0"
  private val playUiVersion = "7.14.0"
  private val playPartialsVersion = "6.1.0"
  private val playAuthorisedFrontendVersion = "7.0.0"
  private val playConfigVersion = "5.0.0"
  private val hmrcTestVersion = "3.0.0"
  private val scalaTestVersion = "3.0.0"
  private val scalaTestPlusVersion = "2.0.0"
  private val pegdownVersion = "1.6.0"
  private val cachingClientVersion = "7.1.0"
  private val mongoCachingVersion = "5.4.0"
  private val playLanguageVersion = "3.4.0"
  private val authClientVersion = "2.6.0"
  private val localTemplateRendererVersion = "2.0.0"
  private val wireMockVersion          = "2.9.0"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "frontend-bootstrap" % frontendBootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "http-caching-client" % cachingClientVersion,
    "uk.gov.hmrc" %% "mongo-caching" % mongoCachingVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
    "uk.gov.hmrc" %% "auth-client" % authClientVersion,
    "uk.gov.hmrc" %% "local-template-renderer" % localTemplateRendererVersion
  )

  trait TestDependencies {
    lazy val scope: String = "test"
    lazy val test : Seq[ModuleID] = Seq.empty
  }

  object Test {
    def apply() = new TestDependencies {
      override lazy val test = Seq(
        "uk.gov.hmrc" %% "hmrctest" % hmrcTestVersion % scope,
        "org.scalatest" %% "scalatest" % scalaTestVersion % scope,
        "org.pegdown" % "pegdown" % pegdownVersion % scope,
        "org.jsoup" % "jsoup" % "1.10.2" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "org.mockito" % "mockito-core" % "2.18.3" % scope
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
        "org.jsoup" % "jsoup" % "1.7.3" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "org.mockito" % "mockito-core" % "2.19.0" % scope,
        "com.github.tomakehurst"  %  "wiremock" % wireMockVersion % scope
      )
    }.test
  }


  def apply() = compile ++ Test() ++ IntegrationTest()
}


