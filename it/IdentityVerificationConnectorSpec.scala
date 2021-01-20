/*
 * Copyright 2021 HM Revenue & Customs
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

import connectors.IdentityVerificationConnector
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.mvc.Http.Status.NOT_FOUND
import uk.gov.hmrc.http.{HeaderCarrier, UpstreamErrorResponse}
import uk.gov.hmrc.play.test.UnitSpec
import utils.{IntegrationBaseSpec, MockedAudit, WiremockHelper}

class IdentityVerificationConnectorSpec extends UnitSpec with GuiceOneServerPerSuite with IntegrationBaseSpec with MockedAudit {

  override val additionalConfiguration = Seq("microservice.services.identity-verification.port" -> WiremockHelper.wiremockPort)

  implicit val hc: HeaderCarrier = HeaderCarrier()

  lazy val missingJourneyId = "1234aa56-7a8a-901a-23aa-aa4a56a78aa9"
  lazy val identityVerificationConnector: IdentityVerificationConnector = app.injector.instanceOf[IdentityVerificationConnector]

  "IdentityVerificationConnector" should {
    "throw an UpstreamErrorResponse with a statusCode of NOT_FOUND" when {
      "IV returns a 404" in {
        stubGet(s"/mdtp/journey/journeyId/$missingJourneyId", NOT_FOUND, s"No journey found for the supplied journeyId = $missingJourneyId")

        val thrown = intercept[UpstreamErrorResponse] {
          await(identityVerificationConnector.identityVerificationResponse(missingJourneyId))
        }

        thrown.statusCode shouldBe NOT_FOUND
      }
    }

  }

}