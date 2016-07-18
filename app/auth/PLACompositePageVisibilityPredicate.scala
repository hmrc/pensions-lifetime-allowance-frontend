/*
 * Copyright 2016 HM Revenue & Customs
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

import java.net.{URLEncoder, URI}

import uk.gov.hmrc.play.frontend.auth.connectors.domain.ConfidenceLevel.L200
import uk.gov.hmrc.play.frontend.auth.{UpliftingIdentityConfidencePredicate, PageVisibilityPredicate, CompositePageVisibilityPredicate}

class PLACompositePageVisibilityPredicate(postSignInRedirectUrl: String,
                                          notAuthorisedRedirectUrl: String,
                                          ivUpliftUrl: String,
                                          twoFactorUrl: String) extends CompositePageVisibilityPredicate {
  override def children: Seq[PageVisibilityPredicate] = Seq (
    new PLAStrongCredentialPredicate(twoFactorURI),
    new UpliftingIdentityConfidencePredicate(L200, ivUpliftURI),
    new PLAUserHasNinoPredicate(ivUpliftURI)
  )

  private val ivUpliftURI: URI =
    new URI(s"${ivUpliftUrl}?origin=PLA&" +
      s"completionURL=${URLEncoder.encode(postSignInRedirectUrl, "UTF-8")}&" +
      s"failureURL=${URLEncoder.encode(notAuthorisedRedirectUrl, "UTF-8")}" +
      s"&confidenceLevel=200")

  private val twoFactorURI: URI =
    new URI(s"${twoFactorUrl}?" +
      s"continue=${URLEncoder.encode(postSignInRedirectUrl, "UTF-8")}&" +
      s"failure=${URLEncoder.encode(notAuthorisedRedirectUrl, "UTF-8")}")
}
