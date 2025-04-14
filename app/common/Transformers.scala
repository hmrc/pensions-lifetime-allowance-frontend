/*
 * Copyright 2023 HM Revenue & Customs
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

import scala.util.{Failure, Success, Try}

object Transformers {

  val stringToOptionalBigDecimal: String => Option[BigDecimal] = input =>
    Try(BigDecimal(input.trim)) match {
      case Success(value) => Some(value)
      case Failure(_)     => None
    }

  val optionalBigDecimalToString: Option[BigDecimal] => String = input =>
    if (input.isEmpty) ""
    else {
      input.get.scale match {
        case 1 => input.getOrElse(BigDecimal(0.0)).setScale(2).toString()
        case _ => input.getOrElse(BigDecimal(0.0)).toString
      }
    }

  val stringToBigDecimal: String => BigDecimal = input =>
    Try(BigDecimal(input.trim)) match {
      case Success(value) => value
      case Failure(_)     => BigDecimal(0)
    }

  val bigDecimalToString: BigDecimal => String = input =>
    input.scale match {
      case 1 => input.setScale(2).toString()
      case _ => input.toString
    }

}
