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

package auth.helpers

import auth.AuthActions
import org.mockito.Mockito.{reset, when}
import org.scalatest.{BeforeAndAfterEach, TestSuite}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.mvc.Results.Unauthorized
import play.api.mvc.{MessagesControllerComponents, Result}
import play.api.test.Injecting

trait AuthMocks extends MockitoSugar with BeforeAndAfterEach with GuiceOneAppPerSuite with Injecting {
  this: TestSuite =>

  val authActions: AuthActions = mock[AuthActions]

  val mcc: MessagesControllerComponents = inject[MessagesControllerComponents]

  def mockAuthSuccess(nino: String = "nino"): Unit =
    when(authActions.authenticateWithNino).thenReturn(MockAuthSuccess(nino, mcc))

  def mockAuthFailure(result: Result = Unauthorized("401 Unauthorized")): Unit =
    when(authActions.authenticateWithNino).thenReturn(MockAuthFailure(result, mcc))

  override def beforeEach(): Unit = {
    super.beforeEach()

    reset(authActions)
  }

}
