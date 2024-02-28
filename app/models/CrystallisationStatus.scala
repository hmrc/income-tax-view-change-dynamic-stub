/*
 * Copyright 2024 HM Revenue & Customs
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

package models

import play.api.UnexpectedException
import play.api.libs.json.{JsValue, Json}

case class CrystallisationStatus(status: String,
                                 nino: String,
                                 taxYearRange: String) {

  val expectedStatusCode: Int = 200

  private def isCrystallised: Boolean = status match {
    case "Crystallised" => true
    case "NonCrystallised" => false
    case _ => throw UnexpectedException(Some("Status can be only Crystallised or Non-Crystallised"))
  }

  def createOverwriteCalculationListUrl: String = {
    if (is1896) {
      s"/income-tax/view/calculations/liability/$taxYearRange/$nino"
    } else {
      s"/income-tax/list-of-calculation-results/$nino?taxYear=20${taxYearRange.takeRight(2)}"
    }
  }

  private def is1896: Boolean = taxYearRange.takeRight(2).toInt >= 24

  private def taxYearField: Option[String] = if (is1896) None else Some("20" + taxYearRange)

  private def calculationTypeField: String = if (is1896) "crystallisation" else "finalDeclaration"

  def makeOverwriteJson: JsValue = {
    Json.arr(
      Json.obj("calculationId" -> "1d35cfe4-cd23-22b2-b074-fae6052024a8",
        "calculationTimestamp" -> s"2024-09-30T09:15:34.0Z",
        "calculationType" -> calculationTypeField) ++
        Json.obj("taxYear" -> taxYearField) ++
        Json.obj("totalIncomeTaxAndNicsDue" -> 15450,
          "crystallised" -> isCrystallised,
          "intentToCrystallise" -> true,
          "crystallisationTimestamp" -> s"2024-09-08T01:03:31.0Z"))
  }

  private def getSchemaIdValue: String = {
    if (is1896) {
      "getCalculationListDetailsSuccess"
    } else {
      "getListCalculationDetailsSuccess"
    }
  }

  def makeOverwriteDataModel: DataModel =
    DataModel(
      _id = createOverwriteCalculationListUrl,
      schemaId = getSchemaIdValue,
      method = "GET",
      status = expectedStatusCode,
      response = Some(makeOverwriteJson)
    )

}