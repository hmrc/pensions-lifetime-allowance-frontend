/*
 * Copyright 2017 HM Revenue & Customs
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


import java.net.URLEncoder

import com.kenshoo.play.metrics.PlayModule
import play.api.test.FakeRequest
import play.api.test.Helpers._
import play.api.http.Status
import uk.gov.hmrc.play.frontend.auth.AuthenticationProviderIds
import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel
import uk.gov.hmrc.play.test.{UnitSpec, WithFakeApplication}

class PLAAuthSpec extends UnitSpec with WithFakeApplication {

  override def bindModules = Seq(new PlayModule)

  "Government Gateway Provider" should {
    "have an account type additional parameter set to individual" in {
      val ggw=new GovernmentGatewayProvider(MockConfig.confirmFPUrl,MockConfig.ggSignInUrl)
      ggw.additionalLoginParameters("accountType") shouldEqual List("individual")
    }
  }

  "Government Gateway Provider" should {
    "have a login url set from its second constructor parameter" in {
      val ggw=new GovernmentGatewayProvider(MockConfig.confirmFPUrl,MockConfig.ggSignInUrl)
      ggw.loginURL shouldEqual MockConfig.ggSignInUrl
    }
  }

  "Government Gateway Provider" should {
    "have a continueURL constructed from its first constructor parameter, encoded" in {
      val ggw=new GovernmentGatewayProvider(mockConfig.confirmFPUrl,mockConfig.ggSignInUrl)
      ggw.continueURL shouldEqual URLEncoder.encode(mockConfig.confirmFPUrl, "UTF-8")
    }
  }

  "Government Gateway Provider" should {
    "handle a session timeout with a redirect" in {
      implicit val fakeRequest = FakeRequest()
      val ggw=new GovernmentGatewayProvider(mockConfig.confirmFPUrl,mockConfig.ggSignInUrl)
      val timeoutHandler = ggw.handleSessionTimeout(fakeRequest)
      status(timeoutHandler) shouldBe Status.SEE_OTHER
    }
  }

  "Verify Provider" should {
    "have a login url set from its second constructor parameter" in {
      val vp = new VerifyProvider(mockConfig.confirmFPUrl, mockConfig.verifySignIn)
      vp.login shouldEqual mockConfig.verifySignIn
    }
  }

  "Verify Provider" should {
    "redirect to login" in {
      implicit val fakeRequest = FakeRequest()
      val vp = new VerifyProvider(mockConfig.confirmFPUrl, mockConfig.verifySignIn)
      val result = vp.redirectToLogin(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
    }
  }

  "PLA Verify Regime" should {
    "return the verify provider" in {
      AuthTestController.PLAVerifyRegime.authenticationType shouldBe AuthTestController.verifyProvider
    }
  }

  "PLAAnyRegime" should {
    "ensure user is authorised if PAYE account exists in session" in {
      AuthTestController.PLAAnyRegime.isAuthorised(authorisedUserAccounts) shouldBe true
    }
  }

  "PLAAnyRegime" should {
    "ensure user is authorised even if PAYE account does not exist in session" in {
      AuthTestController.PLAAnyRegime.isAuthorised(nonAuthorisedUserAccounts) shouldBe true
    }
  }

  "Extract nino of logged in user" should {
    "return \"AB124512C\"" in {
      val user = PLAUser(ggUser.allowedAuthContext)
      user.nino shouldBe Some(nino)
    }
  }

  "Extract previously logged in time of logged in user" should {
    s"return ${ggUser.previouslyLoggedInAt.get}"  in {
      val user = PLAUser(ggUser.allowedAuthContext)
      user.previouslyLoggedInAt shouldBe ggUser.previouslyLoggedInAt
    }
  }

  "Calling PLAStrongCredentialPredicate with an auth context that has weak credentials" should {
    "result in the page being blocked with a redirect to 2FA" in {

      val predicate = new PLAStrongCredentialPredicate(twoFactorURI)
      val authContext = ggUser.tooWeakCredentialsAuthContext

      val result = predicate(authContext, fakeRequest)

      val pageVisibility = await(result)
      pageVisibility.isVisible shouldBe false
    }
  }

  "Calling PLAStrongCredentialPredicate with an auth context that has strong credentials" should {
    "result in page is visible" in {

      val predicate = new PLAStrongCredentialPredicate(twoFactorURI)
      val authContext = ggUser.allowedAuthContext

      val result = predicate(authContext, fakeRequest)

      val pageVisibility = await(result)
      pageVisibility.isVisible shouldBe true
      val nonVisibleResult = await(pageVisibility.nonVisibleResult)
    }
  }

  "Calling PLACompositePageVisibilityPredicate with an auth context that has strong credentials and CL200 confidence" should {
    "result in page is visible" in {

      val predicate = new PLACompositePageVisibilityPredicate(
        mockConfig.confirmFPUrl,
        mockConfig.notAuthorisedRedirectUrl,
        mockConfig.ivUpliftUrl,
        mockConfig.twoFactorUrl)

      val authContext = ggUser.allowedAuthContext

      val result = predicate(authContext, fakeRequest)

      val pageVisibility = await(result)
      pageVisibility.isVisible shouldBe true
    }
  }

  "Calling PLACompositePageVisibilityPredicate with an auth context that has weak credentials and CL200 confidence" should {
    "result in page is not visible" in {

      val predicate = new PLACompositePageVisibilityPredicate(
        mockConfig.confirmFPUrl,
        mockConfig.notAuthorisedRedirectUrl,
        mockConfig.ivUpliftUrl,
        mockConfig.twoFactorUrl)

      val authContext = ggUser.tooWeakCredentialsAuthContext

      val result = predicate(authContext, fakeRequest)

      val pageVisibility = await(result)
      pageVisibility.isVisible shouldBe false
    }
  }

  "Calling PLACompositePageVisibilityPredicate with an auth context that has string credentials and CL50 confidence" should {
    "result in page is not visible" in {

      val predicate = new PLACompositePageVisibilityPredicate(
        mockConfig.confirmFPUrl,
        mockConfig.notAuthorisedRedirectUrl,
        mockConfig.ivUpliftUrl,
        mockConfig.twoFactorUrl)

      val authContext = ggUser.needsUpliftAuthContext
      val result = predicate(authContext, fakeRequest)

      val pageVisibility = await(result)
      pageVisibility.isVisible shouldBe false
    }
  }

  "Calling authenticated async action with no login session" should {
    "result in a redirect to login" in {

      val result = AuthTestController.authorisedAsyncAction(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
    }
  }

  "Calling authenticated sync action with no login session" should {
    "result in a redirect to login" in {

      val result = AuthTestController.authorisedAction(fakeRequest)
      status(result) shouldBe Status.SEE_OTHER
    }
  }

  "Calling authenticated async action with a default GG login session" should {
    "result in an OK status" in {

      val result = AuthTestController.authorisedAsyncAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      status(result) shouldBe Status.OK
    }
  }

  "Calling authenticated sync action with a default GG login session" should {
    "result in an OK status" in {

      val result = AuthTestController.authorisedAction(authenticatedFakeRequest(AuthenticationProviderIds.GovernmentGatewayId))
      status(result) shouldBe Status.OK
    }
  }

  "Calling authenticated action with a default Verify login session" should {
    "result in an OK status" in {

      val result = AuthTestController.authorisedAsyncAction(authenticatedFakeRequest(AuthenticationProviderIds.VerifyProviderId))
      status(result) shouldBe Status.OK
    }
  }

  "Calling authenticated action with a login session with weak credentials" should {
    "result in a redirect status" in {
      val result = AuthTestController.authorisedAsyncAction(weakCredentialsFakeRequest((AuthenticationProviderIds.GovernmentGatewayId)))
      status(result) shouldBe Status.SEE_OTHER
    }
    "redirect to 2FA service" in {
      val result = AuthTestController.authorisedAsyncAction(weakCredentialsFakeRequest((AuthenticationProviderIds.GovernmentGatewayId)))
      val redirectUri = redirectLocation(result)
      redirectUri shouldBe Some(twoFactorURI.toString)
    }
  }

  "Calling authenticated action with a login session with lower confidence" should {
    "result in a redirect status" in {
      val result = AuthTestController.authorisedAsyncAction(lowConfidenceFakeRequest((AuthenticationProviderIds.GovernmentGatewayId)))
      status(result) shouldBe Status.SEE_OTHER
    }
    "redirect to IV uplift service" in {
      val result = AuthTestController.authorisedAsyncAction(lowConfidenceFakeRequest((AuthenticationProviderIds.GovernmentGatewayId)))
      val redirectUri = redirectLocation(result)
      redirectUri shouldBe Some(ivUpliftURI.toString)
    }
  }

  "Calling authenticated controller getAuthenticationProvider with lower confidence level" should {
    "return iv" in {
      AuthTestController.getAuthenticationProvider(ConfidenceLevel.L200) shouldBe "iv"
    }
  }

  "Calling authenticated controller getAuthenticationProvider with CL500 confidence level" should {
    "return verify" in {
      AuthTestController.getAuthenticationProvider(ConfidenceLevel.L500) shouldBe "verify"
    }
  }
}
