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

package models.pla

import utils.{Enumerable, EnumerableInstanceWithKey}

sealed abstract class AmendProtectionResponseStatus(readsWrites: String, toString: String)
    extends EnumerableInstanceWithKey(readsWrites, toString)

object AmendProtectionResponseStatus extends Enumerable.Implicits {

  case object Open      extends AmendProtectionResponseStatus("OPEN", "open")
  case object Dormant   extends AmendProtectionResponseStatus("DORMANT", "dormant")
  case object Withdrawn extends AmendProtectionResponseStatus("WITHDRAWN", "withdrawn")

  private val allValues: Seq[AmendProtectionResponseStatus] =
    Seq(Open, Dormant, Withdrawn)

  implicit val toEnumerable: Enumerable[AmendProtectionResponseStatus] =
    Enumerable(allValues.map(v => v.readsWrites -> v): _*)

}
