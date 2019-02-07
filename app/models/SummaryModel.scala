/*
 * Copyright 2019 HM Revenue & Customs
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

package models

import enums.ApplicationType

case class SummaryModel(protectionType: ApplicationType.Value, invalidRelevantAmount: Boolean, pensionContributionSections: Seq[SummarySectionModel], psoDetailsSections: Seq[SummarySectionModel]) {

  val hasRemoveLink =
    psoDetailsSections.foldLeft(false)((bool, next) =>
      bool match {
        case false => next.rows.exists(_.removeLinkCall.isDefined)
        case _ => bool
      }
    )
}
