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

package testHelpers.ViewSpecHelpers

import akka.stream.Materializer
import config.wiring.{PlaFormPartialRetriever, SessionCookieCryptoFilterWrapper}
import org.scalatest.mockito.MockitoSugar
import play.api.Play
import play.api.test.FakeRequest
import testHelpers.{MockTemplateRenderer, TestConfigHelper}
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}
import utils.PlaTestContext

trait CommonViewSpecHelper extends UnitSpec with CommonMessages with MockitoSugar with WithFakeApplication{

  //TODO

  //mock logger
  implicit val application = fakeApplication
  val sessionCookieCryptoFilterWrapper = mock[SessionCookieCryptoFilterWrapper]
  implicit lazy val fakeRequest = FakeRequest()
  implicit lazy val context = PlaTestContext
  implicit lazy val renderer = MockTemplateRenderer.renderer
  implicit val partialRetriever = new PlaFormPartialRetriever(sessionCookieCryptoFilterWrapper)

}
