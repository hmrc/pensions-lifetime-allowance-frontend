/*
 * Copyright 2020 HM Revenue & Customs
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

import java.net.{URLEncoder, URI}

import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys

package object auth {

  import uk.gov.hmrc.time.DateTimeUtils
  import java.util.UUID

  val mockUsername = "mockuser"
  val mockUserId = "/auth/oid/" + mockUsername

  lazy val fakeRequest = FakeRequest()

  val GovernmentGatewayId = "GGW"

  def authenticatedFakeRequest(provider: String = GovernmentGatewayId,
                               userId: String = mockUserId) =
    FakeRequest().withSession(
      SessionKeys.userId-> userId,
      SessionKeys.sessionId -> s"session-${UUID.randomUUID()}",
      SessionKeys.lastRequestTimestamp -> DateTimeUtils.now.getMillis.toString,
      SessionKeys.token -> "ANYOLDTOKEN",

      SessionKeys.authProvider -> provider
  )

  def weakCredentialsFakeRequest(provider: String = GovernmentGatewayId) =
    authenticatedFakeRequest(provider, "/auth/oid/mockweak")

  def lowConfidenceFakeRequest(provider: String = GovernmentGatewayId) =
  authenticatedFakeRequest(provider, "/auth/oid/mocklowconfidence")
}
