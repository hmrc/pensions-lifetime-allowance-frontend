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

import utils.{Enumerable, EnumerableInstance}

sealed abstract class AmendProtectionResponseStatus(name: String, override val jsonValue: String)
    extends EnumerableInstance(name)

object AmendProtectionResponseStatus extends Enumerable.Implicits {

  case object Open      extends AmendProtectionResponseStatus("Open", "OPEN")
  case object Dormant   extends AmendProtectionResponseStatus("Dormant", "DORMANT")
  case object Withdrawn extends AmendProtectionResponseStatus("Withdrawn", "WITHDRAWN")

  private val allValues: Seq[AmendProtectionResponseStatus] =
    Seq(Open, Dormant, Withdrawn)

  implicit val toEnumerable: Enumerable[AmendProtectionResponseStatus] =
    Enumerable(allValues.map(v => v.jsonValue -> v): _*)

}
