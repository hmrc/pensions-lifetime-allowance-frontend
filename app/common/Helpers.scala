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

package common

import enums.ApplicationStage
import models.ProtectionModel
import models.amend.AmendProtectionModel
import play.api.mvc.Call

object Helpers {

  def createAmendCallIfRequired(model: ProtectionModel): Option[Call] =
    model.asAmendable.map { case (protectionType, status) =>
      controllers.routes.AmendsController.amendsSummary(protectionType, status)
    }

  def createPsoRemoveCall(model: AmendProtectionModel): Call =
    controllers.routes.AmendsRemovePensionSharingOrderController.removePso(model.protectionType, model.status)

  def createAmendCall(model: AmendProtectionModel, applicationSection: ApplicationStage.Value): Call = {
    import ApplicationStage._
    applicationSection match {
      case PensionsTakenBefore =>
        controllers.routes.AmendsPensionTakenBeforeController
          .amendPensionsTakenBefore(model.protectionType, model.status)
      case PensionsWorthBefore =>
        controllers.routes.AmendsPensionWorthBeforeController
          .amendPensionsWorthBefore(model.protectionType, model.status)
      case PensionsTakenBetween =>
        controllers.routes.AmendsPensionTakenBetweenController
          .amendPensionsTakenBetween(model.protectionType, model.status)
      case PensionsUsedBetween =>
        controllers.routes.AmendsPensionUsedBetweenController
          .amendPensionsUsedBetween(model.protectionType, model.status)
      case OverseasPensions =>
        controllers.routes.AmendsOverseasPensionController.amendOverseasPensions(model.protectionType, model.status)
      case CurrentPensions =>
        controllers.routes.AmendsCurrentPensionController.amendCurrentPensions(model.protectionType, model.status)
      case CurrentPsos =>
        controllers.routes.AmendsPensionSharingOrderController.amendPsoDetails(model.protectionType, model.status)
    }
  }

}
