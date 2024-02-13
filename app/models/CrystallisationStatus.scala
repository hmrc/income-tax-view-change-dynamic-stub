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

import scala.util.Try


case class CrystallisationStatus(status: String,
                                 url: String) {

  val expectedStatusCode: Int = 200
  def isCrystallised: Boolean = status match {
    case "Crystallised" => true
    case "NonCrystallised" => false
    case _ => throw UnexpectedException(Some("Status can be only Crystallsied or Non-Crystallised"))
  }

  def makeOverwriteJson: JsValue = {
    Json.obj("calculationId" -> "1d35cfe4-cd23-22b2-b074-fae6052024a8",
      "calculationTimestamp" -> "2023-09-30T09:15:34.0Z",
      "calculationType" -> "crystallisation",
      "totalIncomeTaxAndNicsDue" -> 15450,
      "crystallised" -> isCrystallised,
      "intentToCrystallise" -> true,
      "crystallisationTimestamp" -> "2023-09-08T01:03:31.0Z")
  }

  def makeOverwriteDataModel: DataModel =
    DataModel(
      _id = url,
      schemaId = "getCalculationListDetailsSuccess",
      method = "GET",
      status = expectedStatusCode,
      response = Some(makeOverwriteJson)
    )
}

//class CrystallisationStatus private(val status: String) extends AnyVal {
//
//  def makeOverwriteJson: JsValue = {
//
//    this.status match {
//      case
//    }
//
//  }
//
//}
//
//object CrystallisationStatus {
//
//  def apply(id: String): CrystallisationStatus = {
//    mkCrystallisationStatus(id)
//  }
//
//  def mkCrystallisationStatus(id: String): CrystallisationStatus = {
//    new CrystallisationStatus(id)
//  }
//
//}
