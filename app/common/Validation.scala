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

package common

import play.api.i18n.Messages

object Validation {

  def isMaxTwoDecimalPlaces(amount: BigDecimal): Boolean = {
    amount match {
      case amount if amount.scale <= 2 => true
      case _ => false
    }
  }

  def isPositive(amount: BigDecimal): Boolean = {
    amount match {
      case amount if amount < 0 => false
      case _ => true
    }
  }

  def numberOfPara(number: Int, i: Int = 1): Int = {
    val x: String = "resultCode." + number.toString() + "." + i.toString()
    if(Messages(x) == x){
      i-1
    } else {
      numberOfPara(number, i+1)
    }
  }
}
