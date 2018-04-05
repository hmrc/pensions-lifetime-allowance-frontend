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

import akka.util.Timeout
import org.scalatest._
import org.scalatestplus.play.guice.GuiceGuiceOneServerPerSuite
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.{Application, Configuration}
import play.api.test.DefaultAwaitTimeout
import uk.gov.hmrc.play.test.UnitSpec

import scala.concurrent.duration._

trait IntegrationBaseSpec
  extends UnitSpec
    with GuiceGuiceOneServerPerSuite
    with WiremockHelper
    with BeforeAndAfterEach
    with BeforeAndAfterAll
    with DefaultAwaitTimeout {

  override implicit def defaultAwaitTimeout: Timeout = 5.seconds

  val localHost = "localhost"
  val localPort: Int = port
  val localUrl  = s"http://$localHost:$localPort"

  val additionalConfiguration: Seq[(String, Any)] = Seq.empty

  override implicit lazy val app: Application = new GuiceApplicationBuilder()
    .configure(Configuration("testserver.port" -> s"$localPort"))
    .configure(Configuration("application.router" -> "testOnlyDoNotUseInAppConf.Routes"))
    .configure(Configuration("microservice.services.pla-dynamic-stub.port" -> s"${WiremockHelper.wiremockPort}"))
    .configure(Configuration("auditing.consumer.baseUri.port" -> s"${WiremockHelper.wiremockPort}"))
    .configure(Configuration(additionalConfiguration: _*))
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
