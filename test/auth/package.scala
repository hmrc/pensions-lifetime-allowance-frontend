/*
 * Copyright 2023 HM Revenue & Customs
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

import play.api.test.FakeRequest
import uk.gov.hmrc.http.SessionKeys

package object auth {

  import java.util.UUID

  val mockUsername = "mockuser"
  val mockUserId = "/auth/oid/" + mockUsername

  lazy val fakeRequest = FakeRequest()

  val GovernmentGatewayId = "GGW"

  private case object FakeRequestKeyConsts {
    val SessionId = SessionKeys.sessionId
    val LastRequestTimestamp = SessionKeys.lastRequestTimestamp
    val Token = "token"
    val AuthProvider = "ap"
  }

  def authenticatedFakeRequest(provider: String = GovernmentGatewayId,
                               userId: String = mockUserId) =
    FakeRequest().withSession(
      FakeRequestKeyConsts.SessionId -> s"session-${UUID.randomUUID()}",
      FakeRequestKeyConsts.LastRequestTimestamp -> java.time.Instant.now().truncatedTo(java.time.temporal.ChronoUnit.MILLIS).toString,
      FakeRequestKeyConsts.Token -> "ANYOLDTOKEN",
      FakeRequestKeyConsts.AuthProvider -> provider
    )

  def weakCredentialsFakeRequest(provider: String = GovernmentGatewayId) =
    authenticatedFakeRequest(provider, "/auth/oid/mockweak")

  def lowConfidenceFakeRequest(provider: String = GovernmentGatewayId) =
    authenticatedFakeRequest(provider, "/auth/oid/mocklowconfidence")
}
