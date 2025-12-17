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

package enums

import play.api.Logging
import play.api.libs.json._

sealed trait IdentityVerificationResult

object IdentityVerificationResult extends Logging {

  case object Success extends IdentityVerificationResult

  case object Incomplete extends IdentityVerificationResult

  case object FailedMatching extends IdentityVerificationResult

  case object InsufficientEvidence extends IdentityVerificationResult

  case object LockedOut extends IdentityVerificationResult

  case object UserAborted extends IdentityVerificationResult

  case object Timeout extends IdentityVerificationResult

  case object TechnicalIssue extends IdentityVerificationResult

  case object PreconditionFailed extends IdentityVerificationResult

  case object FailedIV extends IdentityVerificationResult

  case object UnknownOutcome extends IdentityVerificationResult

  val values = Seq(
    Success,
    Incomplete,
    FailedMatching,
    InsufficientEvidence,
    LockedOut,
    UserAborted,
    Timeout,
    TechnicalIssue,
    PreconditionFailed,
    FailedIV,
    UnknownOutcome
  )

  implicit val formats: Format[IdentityVerificationResult] = new Format[IdentityVerificationResult] {
    def reads(json: JsValue): JsResult[IdentityVerificationResult] =
      JsSuccess(IdentityVerificationResult.values.find(_.toString == json.as[String]).getOrElse {
        logger.warn(
          s"No Identity Verification Result for ${json.as[String]} response from auth service. User was taken to the default unauthorised page."
        )
        IdentityVerificationResult.UnknownOutcome
      })
    def writes(result: IdentityVerificationResult): JsValue = JsString(result.toString)
  }

}
