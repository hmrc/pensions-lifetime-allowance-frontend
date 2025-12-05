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

sealed abstract class ProtectionType(name: String, override val jsonValue: String) extends EnumerableInstance(name) {

  def isFixedProtection2016: Boolean =
    this match {
      case ProtectionType.FixedProtection2016    => true
      case ProtectionType.FixedProtection2016LTA => true
      case _                                     => false
    }

}

object ProtectionType extends Enumerable.Implicits {

  case object EnhancedProtection       extends ProtectionType("EnhancedProtection", "ENHANCED PROTECTION")
  case object EnhancedProtectionLTA    extends ProtectionType("EnhancedProtectionLTA", "ENHANCED PROTECTION LTA")
  case object FixedProtection          extends ProtectionType("FixedProtection", "FIXED PROTECTION")
  case object FixedProtection2014      extends ProtectionType("FixedProtection2014", "FIXED PROTECTION 2014")
  case object FixedProtection2014LTA   extends ProtectionType("FixedProtection2014LTA", "FIXED PROTECTION 2014 LTA")
  case object FixedProtection2016      extends ProtectionType("FixedProtection2016", "FIXED PROTECTION 2016")
  case object FixedProtection2016LTA   extends ProtectionType("FixedProtection2016LTA", "FIXED PROTECTION 2016 LTA")
  case object FixedProtectionLTA       extends ProtectionType("FixedProtectionLTA", "FIXED PROTECTION LTA")
  case object IndividualProtection2014 extends ProtectionType("IndividualProtection2014", "INDIVIDUAL PROTECTION 2014")

  case object IndividualProtection2014LTA
      extends ProtectionType("IndividualProtection2014LTA", "INDIVIDUAL PROTECTION 2014 LTA")

  case object IndividualProtection2016 extends ProtectionType("IndividualProtection2016", "INDIVIDUAL PROTECTION 2016")

  case object IndividualProtection2016LTA
      extends ProtectionType("IndividualProtection2016LTA", "INDIVIDUAL PROTECTION 2016 LTA")

  case object InternationalEnhancementS221
      extends ProtectionType("InternationalEnhancementS221", "INTERNATIONAL ENHANCEMENT (S221)")

  case object InternationalEnhancementS224
      extends ProtectionType("InternationalEnhancementS224", "INTERNATIONAL ENHANCEMENT (S224)")

  case object PensionCreditRights  extends ProtectionType("PensionCreditRights", "PENSION CREDIT RIGHTS")
  case object PrimaryProtection    extends ProtectionType("PrimaryProtection", "PRIMARY PROTECTION")
  case object PrimaryProtectionLTA extends ProtectionType("PrimaryProtectionLTA", "PRIMARY PROTECTION LTA")

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
    Enumerable(values.map(v => v.jsonValue -> v): _*)

}
