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

package constructors

import models.ProtectionModel
import models.amendModels.AmendsGAModel

object AmendsGAConstructor {

  def identifyAmendsChanges(updated: ProtectionModel, original: ProtectionModel): AmendsGAModel ={
    val current: Option[String] = if(updated.uncrystallisedRights != original.uncrystallisedRights) Some("UpdatedValue") else None

    val before: Option[String] = gaAction(updated.preADayPensionInPayment, original.preADayPensionInPayment)

    val between: Option[String] = gaAction(updated.postADayBenefitCrystallisationEvents, original.postADayBenefitCrystallisationEvents)

    val overseas: Option[String] = gaAction(updated.nonUKRights, original.nonUKRights)

    val pso: Option[String] = if(updated.pensionDebits.isDefined) Some("addedPSO") else None
    AmendsGAModel(current,before,between,overseas,pso)
  }

  def gaAction(updated: Option[Double], original: Option[Double]): Option[String] ={
    if(updated != original){
      if(!updated.contains(0.0) && !original.contains(0.0)) Some("UpdatedValue")
      else if(updated.contains(0.0)) Some("ChangedToNo")
      else Some("ChangedToYes")
    } else None
  }

}
