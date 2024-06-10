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

package utils

import models.{DataModel, TaxYear}
import play.api.libs.json.Reads.of
import play.api.libs.json.{JsArray, JsNumber, JsObject, JsValue, Json, Reads, __}

trait PoaUtils {

  def extractNino(json: JsValue): Option[String] = {
    (json \ "nino").asOpt[String]
  }

  def extractPoAAmount(json: JsValue): Option[BigDecimal] = {
    (json \ "amount").asOpt[BigDecimal]
  }

  def extractTaxYear(json: JsValue): Option[String] = {
    (json \ "taxYear").asOpt[String]
  }

  def extractChargeRef(json: JsValue): Option[String] = {
    (json \ "financialDetails" \ 0 \ "chargeReference").asOpt[String]
  }

  def getFinancialDetailsUrl(nino: String, taxYear: TaxYear): String = {
    s"/enterprise/02.00.00/financial-data/NINO/$nino/ITSA?dateFrom=${taxYear.startYear}-04-06&dateTo=${taxYear.endYear}-04-05&onlyOpenItems=false&includeLocks=true&calculateAccruedInterest=true&removePOA=false&customerPaymentInformation=true&includeStatistical=false"
  }

  def getChargeHistoryUrl(nino: String, chargeReference: String) = {
    s"/cross-regime/charges/NINO/$nino/ITSA?chargeReference=$chargeReference"
  }

  def transformDocDetails(amount: Int): Reads[JsObject] = {
    (__ \ "documentDetails").json.update(
      of[JsArray].map {
        case JsArray(arr) =>
          JsArray(arr.map(item => item.transform(transformAmount(amount)).getOrElse(item)))
      }
    )
  }

  def transformAmount(amount: Int): Reads[JsObject] = {
    (__ \ "totalAmount").json.update(
      of[JsNumber].map {
        case JsNumber(_) =>
          JsNumber(amount)
      }
    )
  }

  def transformFinDetails(chargeRef: String): Reads[JsObject] = {
    (__ \ "financialDetails").json.update(
      of[JsArray].map {
        case JsArray(arr) =>
          JsArray(arr.map(item => item.transform(transformChargeRef(chargeRef)).getOrElse(item)))
      }
    )
  }

  def transformChargeRef(chargeRef: String): Reads[JsObject] = {
    __.json.update(
      __.read[JsObject].map { o => o ++ Json.obj("chargeReference" -> chargeRef) }
    )
  }

  def getFinDetailsDataModel(request: JsValue, url: String): DataModel = DataModel(
    _id = url,
    schemaId = "getFinancialDetailsSuccess",
    method = "GET",
    status = 200,
    response = Some(request)
  )

  def getChargeHistoryDataModel(url: String, nino: String, amount: Int): DataModel = DataModel(
    _id = url,
    schemaId = "getChargesHistorySuccess",
    method = "GET",
    status = 200,
    response = Some(chargeHistoryRequest(nino, amount))
  )

  def chargeHistoryRequest(nino: String, amount: Int): JsValue = Json.obj(
    "idType" -> "NINO",
    "idValue" -> nino,
    "regimeType" -> "ITSA",
    "chargeHistoryDetails" -> Json.arr(
      Json.obj(
        "taxYear" -> "2019",
        "documentId" -> "12345678",
        "documentDate" -> "2018-02-14",
        "documentDescription" -> "ITSA - POA 1",
        "totalAmount" -> amount,
        "reversalDate" -> "2019-02-14",
        "reversalReason" -> "Customer Request",
        "poaAdjustmentReason" -> "002"
      ),
      Json.obj(
        "taxYear" -> "2019",
        "documentId" -> "12345678",
        "documentDate" -> "2018-02-14",
        "documentDescription" -> "ITSA - POA 2",
        "totalAmount" -> amount,
        "reversalDate" -> "2019-02-14",
        "reversalReason" -> "Customer Request",
        "poaAdjustmentReason" -> "002"
      )
    )
  )

}
