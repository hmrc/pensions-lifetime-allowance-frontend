/*
 * Copyright 2026 HM Revenue & Customs
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

package auth

import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.{ActionBuilder, AnyContent, MessagesActionBuilder, MessagesControllerComponents}

class AuthActionsSpec extends AnyWordSpec with MockitoSugar with BeforeAndAfterEach with Matchers {

  private val messagesControllerComponents: MessagesControllerComponents = mock[MessagesControllerComponents]
  private val authenticateWithNino: AuthenticateWithNino                 = mock[AuthenticateWithNino]

  private val messagesActionBuilder: MessagesActionBuilder = mock[MessagesActionBuilder]

  private val authenticatedRequestActionBuilder: ActionBuilder[AuthenticatedRequest, AnyContent] =
    mock[ActionBuilder[AuthenticatedRequest, AnyContent]]

  private val authActions =
    new AuthActions(
      messagesControllerComponents = messagesControllerComponents,
      authenticateWithNinoAction = authenticateWithNino
    )

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(messagesControllerComponents)
    reset(authenticateWithNino)
    reset(messagesActionBuilder)
  }

  "AuthActions.authenticateWithNino" should {
    "call AuthenticateWithNino" in {
      when(messagesControllerComponents.messagesActionBuilder).thenReturn(messagesActionBuilder)
      when(messagesActionBuilder.andThen[AuthenticatedRequest](any())).thenReturn(authenticatedRequestActionBuilder)

      authActions.authenticateWithNino shouldBe authenticatedRequestActionBuilder

      verify(messagesActionBuilder).andThen(authenticateWithNino)
    }
  }

}
