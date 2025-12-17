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

package constructors

import models.amend.{AmendProtectionFields, AmendsGAModel}

object AmendsGAConstructor {

  def identifyAmendsChanges(updated: AmendProtectionFields, original: AmendProtectionFields): AmendsGAModel = {
    val current: Option[String] =
      if (updated.uncrystallisedRightsAmount != original.uncrystallisedRightsAmount) Some("UpdatedValue") else None

    val before: Option[String] = gaAction(updated.preADayPensionInPaymentAmount, original.preADayPensionInPaymentAmount)

    val between: Option[String] =
      gaAction(updated.postADayBenefitCrystallisationEventAmount, original.postADayBenefitCrystallisationEventAmount)

    val overseas: Option[String] = gaAction(updated.nonUKRightsAmount, original.nonUKRightsAmount)

    val pso: Option[String] = if (updated.pensionDebit.isDefined) Some("addedPSO") else None
    AmendsGAModel(current, before, between, overseas, pso)
  }

  private def gaAction(updated: Option[Double], original: Option[Double]): Option[String] =
    if (updated != original) {
      if (!updated.contains(0.0) && !original.contains(0.0)) Some("UpdatedValue")
      else if (updated.contains(0.0)) Some("ChangedToNo")
      else Some("ChangedToYes")
    } else None

}
