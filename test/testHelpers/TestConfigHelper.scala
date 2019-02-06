/*
 * Copyright 2019 HM Revenue & Customs
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

package testHelpers

import akka.stream.Materializer
import config.LocalTemplateRenderer
import config.wiring.PlaFormPartialRetriever
import connectors.{KeyStoreConnector, PLAConnector}
import play.api.{Configuration, Environment, Play}
import org.scalatest.mockito.MockitoSugar
import play.api.i18n.Messages
import services.MetricsService
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

trait TestConfigHelper extends MockitoSugar with UnitSpec with WithFakeApplication {
  override lazy val fakeApplication = Play.current
  val config = mock[Configuration]
  val env = mock[Environment]
  implicit val mat: Materializer = Play.materializer(fakeApplication)
}

trait TestControllerHelper extends MockitoSugar with TestConfigHelper{
  val keyStoreConnector = mock[KeyStoreConnector]
  val plaConnector = mock[PLAConnector]
  implicit val partialRetriever = mock[PlaFormPartialRetriever]
  implicit val templateRenderer = mock[LocalTemplateRenderer]
}

