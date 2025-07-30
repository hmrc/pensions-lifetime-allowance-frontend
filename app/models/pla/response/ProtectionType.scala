/*
 * Copyright 2025 HM Revenue & Customs
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

package models.pla.response

import utils.{Enumerable, WithName}

sealed trait ProtectionType

object ProtectionType extends Enumerable.Implicits {

  override val jsonErrorMessage: String = "error.protectionType"

  case object FixedProtection2016          extends WithName("FIXED PROTECTION 2016") with ProtectionType
  case object IndividualProtection2014     extends WithName("INDIVIDUAL PROTECTION 2014") with ProtectionType
  case object IndividualProtection2016     extends WithName("INDIVIDUAL PROTECTION 2016") with ProtectionType
  case object PrimaryProtection            extends WithName("PRIMARY PROTECTION") with ProtectionType
  case object EnhancedProtection           extends WithName("ENHANCED PROTECTION") with ProtectionType
  case object FixedProtection              extends WithName("FIXED PROTECTION") with ProtectionType
  case object FixedProtection2014          extends WithName("FIXED PROTECTION 2014") with ProtectionType
  case object PensionCreditRights          extends WithName("PENSION CREDIT RIGHTS") with ProtectionType
  case object InternationalEnhancementS221 extends WithName("INTERNATIONAL ENHANCEMENT (S221)") with ProtectionType
  case object InternationalEnhancementS224 extends WithName("INTERNATIONAL ENHANCEMENT (S224)") with ProtectionType
  case object FixedProtection2016LTA       extends WithName("FIXED PROTECTION 2016 LTA") with ProtectionType
  case object IndividualProtection2014LTA  extends WithName("INDIVIDUAL PROTECTION 2014 LTA") with ProtectionType
  case object IndividualProtection2016LTA  extends WithName("INDIVIDUAL PROTECTION 2016 LTA") with ProtectionType
  case object PrimaryProtectionLTA         extends WithName("PRIMARY PROTECTION LTA") with ProtectionType
  case object EnhancedProtectionLTA        extends WithName("ENHANCED PROTECTION LTA") with ProtectionType
  case object FixedProtectionLTA           extends WithName("FIXED PROTECTION LTA") with ProtectionType
  case object FixedProtection2014LTA       extends WithName("FIXED PROTECTION 2014 LTA") with ProtectionType

  val values: Seq[ProtectionType] = Seq(
    FixedProtection2016,
    IndividualProtection2014,
    IndividualProtection2016,
    PrimaryProtection,
    EnhancedProtection,
    FixedProtection,
    FixedProtection2014,
    PensionCreditRights,
    InternationalEnhancementS221,
    InternationalEnhancementS224,
    FixedProtection2016LTA,
    IndividualProtection2014LTA,
    IndividualProtection2016LTA,
    PrimaryProtectionLTA,
    EnhancedProtectionLTA,
    FixedProtectionLTA,
    FixedProtection2014LTA
  )

  implicit val enumerable: Enumerable[ProtectionType] =
    Enumerable(values.map(v => v.toString -> v): _*)

}
