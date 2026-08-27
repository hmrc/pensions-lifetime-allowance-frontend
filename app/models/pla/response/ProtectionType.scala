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

import util.{JsonEnum, JsonEnumFormat}

enum ProtectionType(
    override val toString: String,
    override val jsonString: String
) extends JsonEnum {
  case EnhancedProtection       extends ProtectionType("EnhancedProtection", "ENHANCED PROTECTION")
  case EnhancedProtectionLTA    extends ProtectionType("EnhancedProtectionLTA", "ENHANCED PROTECTION LTA")
  case FixedProtection          extends ProtectionType("FixedProtection", "FIXED PROTECTION")
  case FixedProtection2014      extends ProtectionType("FixedProtection2014", "FIXED PROTECTION 2014")
  case FixedProtection2014LTA   extends ProtectionType("FixedProtection2014LTA", "FIXED PROTECTION 2014 LTA")
  case FixedProtection2016      extends ProtectionType("FixedProtection2016", "FIXED PROTECTION 2016")
  case FixedProtection2016LTA   extends ProtectionType("FixedProtection2016LTA", "FIXED PROTECTION 2016 LTA")
  case FixedProtectionLTA       extends ProtectionType("FixedProtectionLTA", "FIXED PROTECTION LTA")
  case IndividualProtection2014 extends ProtectionType("IndividualProtection2014", "INDIVIDUAL PROTECTION 2014")

  case IndividualProtection2014LTA
      extends ProtectionType("IndividualProtection2014LTA", "INDIVIDUAL PROTECTION 2014 LTA")

  case IndividualProtection2016 extends ProtectionType("IndividualProtection2016", "INDIVIDUAL PROTECTION 2016")

  case IndividualProtection2016LTA
      extends ProtectionType("IndividualProtection2016LTA", "INDIVIDUAL PROTECTION 2016 LTA")

  case InternationalEnhancementS221
      extends ProtectionType("InternationalEnhancementS221", "INTERNATIONAL ENHANCEMENT (S221)")

  case InternationalEnhancementS224
      extends ProtectionType("InternationalEnhancementS224", "INTERNATIONAL ENHANCEMENT (S224)")

  case PensionCreditRights  extends ProtectionType("PensionCreditRights", "PENSION CREDIT RIGHTS")
  case PrimaryProtection    extends ProtectionType("PrimaryProtection", "PRIMARY PROTECTION")
  case PrimaryProtectionLTA extends ProtectionType("PrimaryProtectionLTA", "PRIMARY PROTECTION LTA")

  def isFixedProtection2016: Boolean =
    this match {
      case ProtectionType.FixedProtection2016    => true
      case ProtectionType.FixedProtection2016LTA => true
      case _                                     => false
    }

}

object ProtectionType extends JsonEnumFormat[ProtectionType]
