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

import utils.{Enumerable, EnumerableInstance}

sealed abstract class ProtectionType(value: String) extends EnumerableInstance(value)

object ProtectionType extends Enumerable.Implicits {

  case object FixedProtection2016          extends ProtectionType("FIXED PROTECTION 2016")
  case object IndividualProtection2014     extends ProtectionType("INDIVIDUAL PROTECTION 2014")
  case object IndividualProtection2016     extends ProtectionType("INDIVIDUAL PROTECTION 2016")
  case object PrimaryProtection            extends ProtectionType("PRIMARY PROTECTION")
  case object EnhancedProtection           extends ProtectionType("ENHANCED PROTECTION")
  case object FixedProtection              extends ProtectionType("FIXED PROTECTION")
  case object FixedProtection2014          extends ProtectionType("FIXED PROTECTION 2014")
  case object PensionCreditRights          extends ProtectionType("PENSION CREDIT RIGHTS")
  case object InternationalEnhancementS221 extends ProtectionType("INTERNATIONAL ENHANCEMENT (S221)")
  case object InternationalEnhancementS224 extends ProtectionType("INTERNATIONAL ENHANCEMENT (S224)")
  case object FixedProtection2016LTA       extends ProtectionType("FIXED PROTECTION 2016 LTA")
  case object IndividualProtection2014LTA  extends ProtectionType("INDIVIDUAL PROTECTION 2014 LTA")
  case object IndividualProtection2016LTA  extends ProtectionType("INDIVIDUAL PROTECTION 2016 LTA")
  case object PrimaryProtectionLTA         extends ProtectionType("PRIMARY PROTECTION LTA")
  case object EnhancedProtectionLTA        extends ProtectionType("ENHANCED PROTECTION LTA")
  case object FixedProtectionLTA           extends ProtectionType("FIXED PROTECTION LTA")
  case object FixedProtection2014LTA       extends ProtectionType("FIXED PROTECTION 2014 LTA")

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
