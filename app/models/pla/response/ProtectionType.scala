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

import utils.{Enumerable, EnumerableInstanceWithKey}

sealed abstract class ProtectionType(readsWrites: String, toString: String)
    extends EnumerableInstanceWithKey(readsWrites, toString)

object ProtectionType extends Enumerable.Implicits {

  case object EnhancedProtection          extends ProtectionType("ENHANCED PROTECTION", "enhanced")
  case object EnhancedProtectionLTA       extends ProtectionType("ENHANCED PROTECTION LTA", "enhancedLTA")
  case object FixedProtection             extends ProtectionType("FIXED PROTECTION", "fixed")
  case object FixedProtection2014         extends ProtectionType("FIXED PROTECTION 2014", "FP2014")
  case object FixedProtection2014LTA      extends ProtectionType("FIXED PROTECTION 2014 LTA", "FP2014LTA")
  case object FixedProtection2016         extends ProtectionType("FIXED PROTECTION 2016", "FP2016")
  case object FixedProtection2016LTA      extends ProtectionType("FIXED PROTECTION 2016 LTA", "FP2016LTA")
  case object FixedProtectionLTA          extends ProtectionType("FIXED PROTECTION LTA", "fixedLTA")
  case object IndividualProtection2014    extends ProtectionType("INDIVIDUAL PROTECTION 2014", "IP2014")
  case object IndividualProtection2014LTA extends ProtectionType("INDIVIDUAL PROTECTION 2014 LTA", "IP2014LTA")
  case object IndividualProtection2016    extends ProtectionType("INDIVIDUAL PROTECTION 2016", "IP2016")
  case object IndividualProtection2016LTA extends ProtectionType("INDIVIDUAL PROTECTION 2016 LTA", "IP2016LTA")

  case object InternationalEnhancementS221
      extends ProtectionType("INTERNATIONAL ENHANCEMENT (S221)", "internationalEnhancementS221")

  case object InternationalEnhancementS224
      extends ProtectionType("INTERNATIONAL ENHANCEMENT (S224)", "internationalEnhancementS224")

  case object PensionCreditRights  extends ProtectionType("PENSION CREDIT RIGHTS", "pensionCreditRights")
  case object PrimaryProtection    extends ProtectionType("PRIMARY PROTECTION", "primary")
  case object PrimaryProtectionLTA extends ProtectionType("PRIMARY PROTECTION LTA", "primaryLTA")

  val values: Seq[ProtectionType] = Seq(
    EnhancedProtection,
    EnhancedProtectionLTA,
    FixedProtection,
    FixedProtection2014,
    FixedProtection2014LTA,
    FixedProtection2016,
    FixedProtection2016LTA,
    FixedProtectionLTA,
    IndividualProtection2014,
    IndividualProtection2014LTA,
    IndividualProtection2016,
    IndividualProtection2016LTA,
    InternationalEnhancementS221,
    InternationalEnhancementS224,
    PensionCreditRights,
    PrimaryProtection,
    PrimaryProtectionLTA
  )

  implicit val enumerable: Enumerable[ProtectionType] =
    Enumerable(values.map(v => v.readsWrites -> v): _*)

}
