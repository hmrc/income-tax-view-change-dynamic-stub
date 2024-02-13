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

import play.api.libs.json.{JsValue, Json}

case class ItsaStatus(status: String,
                      url: String,
                      taxYearRange: String) {

  val expectedStatusCode: Int = 200

  def itsaStatusLong: String = status match {
    case "NotSet" => "No Status"
    case "Voluntary" => "MTD Voluntary"
    case "Mandatory" => "MTD Mandated"
    case "Annual" => "Annual"
  }

  def statusReason: String = status match {
    case "NotSet" => "Sign up - return available"
    case _ => "Sign up - no return available"
  }

  private val jsonTaxYearRange: String = s"20$taxYearRange"

  def makeOverwriteJson: JsValue = {
    Json.obj(
      "taxYear" -> jsonTaxYearRange,
      "itsaStatusDetails" -> Json.arr(
        "submittedOn" -> "2022-01-10T06:14:00Z",
        "status" -> itsaStatusLong,
        "statusReason" -> statusReason
      )
    )
  }

  def makeOverwriteDataModel: DataModel =
    DataModel(
      _id = url,
      schemaId = "getITSAStatusSuccess",
      method = "GET",
      status = expectedStatusCode,
      response = Some(makeOverwriteJson)
    )

}
