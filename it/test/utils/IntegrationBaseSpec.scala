/*
 * Copyright 2018 HM Revenue & Customs
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

package utils

import org.apache.pekko.util.Timeout
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, OptionValues}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.{Application, Configuration}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.DefaultAwaitTimeout

import scala.concurrent.duration._

trait IntegrationBaseSpec
  extends AnyWordSpecLike with Matchers with OptionValues
    with GuiceOneServerPerSuite
    with WiremockHelper
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with DefaultAwaitTimeout {

  override implicit def defaultAwaitTimeout: Timeout = 5.seconds

  val localHost = "localhost"
  val localPort: Int = port
  val localUrl  = s"http://$localHost:$localPort"

  def defaultConfiguration: Configuration = Configuration(
    "testserver.port" -> s"$localPort",
      "application.router" -> "testOnlyDoNotUseInAppConf.Routes",
      "microservice.services.pla-dynamic-stub.port" -> s"${WiremockHelper.wiremockPort}",
      "auditing.consumer.baseUri.port" -> s"${WiremockHelper.wiremockPort}"
  )

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(defaultConfiguration)
    .build()

  override def beforeEach() = {
    resetWiremock()
  }

  override def beforeAll() = {
    super.beforeAll()
    startWiremock()
  }

  override def afterAll() = {
    stopWiremock()
    super.afterAll()
  }
}
