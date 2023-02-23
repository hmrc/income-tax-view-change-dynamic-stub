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

package controllers.helpers

import models.{CalcSuccessReponse, DataModel}
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{ControllerComponents, MessagesControllerComponents}
import play.api.test.Helpers.stubMessagesControllerComponents

trait DataHelper {

  lazy val mockCC: MessagesControllerComponents = stubMessagesControllerComponents()

  implicit val calcSuccessReponseWrites: OWrites[CalcSuccessReponse] = Json.writes[CalcSuccessReponse]

  val calcResponse = CalcSuccessReponse(
    calculationId = "041f7e4d-87d9-4d4a-a296-3cfbdf",
    calculationTimestamp = "2018-07-13T12:13:48.763Z",
    calculationType = "inYear",
    requestedBy = "customer",
    year = 2019,
    fromDate = "2018-04-06",
    toDate = "2019-04-05",
    totalIncomeTaxAndNicsDue = BigDecimal("1250.00"),
    intentToCrystallise = false,
    crystallised = true
  )
  lazy val successWithBodyModel: DataModel = DataModel(
    _id = "test",
    schemaId = "testID2",
    method = "GET",
    status = OK,
    response = Some(Json.parse(Json.toJson(calcResponse).toString()))
  )

  lazy val successWithEmptyBody: DataModel = DataModel(
    _id = "test",
    schemaId = "testID2",
    method = "GET",
    status = BAD_REQUEST,
    response = None
  )

  val ninoTest: String = "1111AAAA"
  val ninoWithWrongFormat: String = "111AAA"
  val calculationIdTest : String =  "041f7e4d-87d9-4d4a-a296-3cfbdf"
}
