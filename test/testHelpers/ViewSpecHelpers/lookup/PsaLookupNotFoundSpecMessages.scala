/*
 * Copyright 2018 HM Revenue & Customs
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

package testHelpers.ViewSpecHelpers.lookup

trait PsaLookupNotFoundSpecMessages {
  val logoText = "GOV.UK"
  val hmrcText = "HM Revenue & Customs"
  val checkDetailsText = "Check the details you entered"
  val tableRowOneElementOneText = "Scheme Administrator Reference:"
  val tableRowOneElementTwoText = "CHECK"
  val tableRowTwoElementOneText = "Protection Notification Number:"
  val tableRowTwoElementTwoText = "REF"
  val tableRowThreeElementOneText = "Checked on:"
  val tableRowThreeElementTwoText = "timestamp"
  val detailsText = "The details you entered don't match an existing protection certificate."
  val causesText = "This could be because you've:"
  val causeOneText = "been given the wrong information by your client"
  val causeTwoText = "entered one of the numbers incorrectly"
  val suggestionsText = "Check the information you've been given and try again."
  val copyrightText = "© Crown Copyright"
  val pdfLinkText = "Download a PDF of these results"
  val startAgainText = "Try again"
}
