/*
 * Copyright 2016 HM Revenue & Customs
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
    "pensionsTaken"     -> controllers.routes.IP2016Controller.pensionsTaken(),
    "ip14PensionsTaken" -> controllers.routes.IP2014Controller.ip14PensionsTaken(),

    "pensionsTakenBefore"     -> controllers.routes.IP2016Controller.pensionsTakenBefore(),
    "ip14PensionsTakenBefore" -> controllers.routes.IP2014Controller.ip14PensionsTakenBefore(),

    "pensionsTakenBetween"     -> controllers.routes.IP2016Controller.pensionsTakenBetween(),
    "ip14PensionsTakenBetween" -> controllers.routes.IP2014Controller.ip14PensionsTakenBetween(),

    "overseasPensions"     -> controllers.routes.IP2016Controller.overseasPensions(),
    "ip14OverseasPensions" -> controllers.routes.IP2014Controller.ip14OverseasPensions(),

    "currentPensions"     -> controllers.routes.IP2016Controller.currentPensions(),
    "ip14CurrentPensions" -> controllers.routes.IP2014Controller.ip14CurrentPensions(),

    "pensionDebits"       -> controllers.routes.IP2016Controller.pensionDebits(),
    "ip14PensionDebits"   -> controllers.routes.IP2014Controller.ip14PensionDebits(),

//    "numberOfPSOs"       -> controllers.routes.IP2016Controller.numberOfPSOs(),
//    "ip14NumberOfPSOs"   -> controllers.routes.IP2014Controller.ip14NumberOfPSOs(),
//
    "psoDetails"     -> controllers.routes.IP2016Controller.psoDetails,
    "ip14PsoDetails" -> controllers.routes.IP2014Controller.ip14PsoDetails
//
//    "psoDetails2"     -> controllers.routes.IP2016Controller.psoDetails("2"),
//    "ip14PsoDetails2" -> controllers.routes.IP2014Controller.ip14PsoDetails("2"),
//
//    "psoDetails3"     -> controllers.routes.IP2016Controller.psoDetails("3"),
//    "ip14PsoDetails3" -> controllers.routes.IP2014Controller.ip14PsoDetails("3"),
//
//    "psoDetails4"     -> controllers.routes.IP2016Controller.psoDetails("4"),
//    "ip14PsoDetails4" -> controllers.routes.IP2014Controller.ip14PsoDetails("4"),
//
//    "psoDetails5"     -> controllers.routes.IP2016Controller.psoDetails("5"),
//    "ip14PsoDetails5" -> controllers.routes.IP2014Controller.ip14PsoDetails("5")
    )

  def get(name: String): Option[Call] = callMap.get(name)
}
