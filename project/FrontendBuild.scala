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

  private val bootstrapVersion = "0.40.0"
  private val govukTemplateVersion = "5.35.0-play-26"
  private val playUiVersion = "7.40.0-play-26"
  private val playPartialsVersion = "6.9.0-play-26"
  private val hmrcTestVersion = "3.8.0-play-26"
  private val scalaTestVersion = "3.0.7"
  private val scalaTestPlusVersion = "3.1.2"
  private val pegdownVersion = "1.6.0"
  private val cachingClientVersion = "8.3.0"
  private val mongoCachingVersion = "6.4.0-play-26"
  private val playLanguageVersion = "3.4.0"
  private val authClientVersion = "2.21.0-play-26"
  private val localTemplateRendererVersion = "2.4.0"
  private val wireMockVersion          = "2.23.2"
  private val reactiveMongoVersion = "7.20.0-play-26"

  val compile = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-play-26" % bootstrapVersion,
    "uk.gov.hmrc" %% "play-partials" % playPartialsVersion,
    "uk.gov.hmrc" %% "http-caching-client" % cachingClientVersion,
    "uk.gov.hmrc" %% "mongo-caching" % mongoCachingVersion,
    "uk.gov.hmrc" %% "play-language" % playLanguageVersion,
    "uk.gov.hmrc" %% "auth-client" % authClientVersion,
    "uk.gov.hmrc" %% "local-template-renderer" % localTemplateRendererVersion,
    "uk.gov.hmrc" %% "govuk-template" % govukTemplateVersion,
    "uk.gov.hmrc" %% "play-ui" % playUiVersion,
    "uk.gov.hmrc" %% "simple-reactivemongo" % reactiveMongoVersion
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
        "org.jsoup" % "jsoup" % "1.12.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.mockito" % "mockito-core" % "2.27.0" % scope
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
        "org.jsoup" % "jsoup" % "1.12.1" % scope,
        "com.typesafe.play" %% "play-test" % PlayVersion.current % scope,
        "org.scalatestplus.play" %% "scalatestplus-play" % scalaTestPlusVersion % scope,
        "org.mockito" % "mockito-core" % "2.27.0" % scope,
        "com.github.tomakehurst"  %  "wiremock" % wireMockVersion % scope, 
        "com.github.tomakehurst" % "wiremock-jre8" % "2.23.2" % "test,it"

      )
    }.test
  }


  def apply() = compile ++ Test() ++ IntegrationTest()
}


