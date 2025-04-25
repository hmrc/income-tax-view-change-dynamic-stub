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

import play.api.http.Status.OK
import play.api.libs.json.{JsValue, Json}

case class ItsaStatus(
    status:   String,
    url:      String,
    taxYear:  TaxYear,
    isHipApi: Boolean) {

  val expectedStatusCode: Int = OK

  def statusReason: String =
    status match {
      case "NoStatus" if isHipApi => "00"
      case "NoStatus"             => "Sign up - return available"
      case _ if isHipApi          => "01"
      case _                      => "Sign up - no return available"

    }

  val statusKeyMap = Map(
    "No Status"        -> "00",
    "MTD Mandated"     -> "01",
    "MTD Voluntary"    -> "02",
    "Annual"           -> "03",
    "Digitally Exempt" -> "04",
    "Dormant"          -> "05",
    "MTD Exempt"       -> "99"
  )

  def makeOverwriteJson: JsValue = {
    Json.arr(
      Json.obj(
        "taxYear" -> taxYear.rangeLong,
        "itsaStatusDetails" -> Json.arr(
          Json.obj(
            "submittedOn"  -> s"2024-01-10T06:14:00Z",
            "status"       -> { if (isHipApi) statusKeyMap(status) else status },
            "statusReason" -> statusReason
          )
        )
      )
    )
  }

  def makeOverwriteDataModel: DataModel = {
    DataModel(
      _id = url,
      schemaId = "getITSAStatusSuccess",
      method = "GET",
      status = expectedStatusCode,
      response = Some(makeOverwriteJson)
    )
  }

}
