/*
 * Copyright 2021 HM Revenue & Customs
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


object Exceptions extends Exceptions

trait Exceptions {

  class OptionNotDefinedException(val functionName: String, optionName: String, applicationType: String) extends Exception(
    s"Option not found for $optionName in $functionName for application type $applicationType"
  )

  class RequiredValueNotDefinedException(val functionName: String, optionName: String) extends Exception(
    s"Value not found for $optionName in $functionName"
  )

  class RequiredValueNotDefinedForNinoException(val functionName: String, optionName: String, nino: String) extends Exception(
    s"Value not found for $optionName in $functionName with nino: $nino"
  )

  class RequiredNotFoundProtectionTypeException(val functionName: String) extends Exception(
    s"Value not found for protection type in $functionName"
  )

}
