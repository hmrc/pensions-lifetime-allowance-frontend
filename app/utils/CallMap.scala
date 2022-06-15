/*
 * Copyright 2022 HM Revenue & Customs
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

package utils


import play.api.mvc.Call

object CallMap {
  private val callMap: Map[String, Call] = Map(
    "pensionsTaken"     -> controllers.routes.IP2016Controller.pensionsTaken,

    "pensionsTakenBefore"     -> controllers.routes.IP2016Controller.pensionsTakenBefore,

    "pensionsTakenBetween"     -> controllers.routes.IP2016Controller.pensionsTakenBetween,

    "overseasPensions"     -> controllers.routes.IP2016Controller.overseasPensions,

    "currentPensions"     -> controllers.routes.IP2016Controller.currentPensions,

    "pensionDebits"       -> controllers.routes.IP2016Controller.pensionDebits,

    "psoDetails"     -> controllers.routes.IP2016Controller.psoDetails,

    "removePsoDetails" -> controllers.routes.IP2016Controller.removePsoDetails
    )

  def get(name: String): Option[Call] = callMap.get(name)
}
