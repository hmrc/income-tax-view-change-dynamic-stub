/*
 * Copyright 2022 HM Revenue & Customs
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

package controllers

import mocks.{MockDataRepository, MockSchemaValidation}
import models.{CalcSuccessReponse, DataModel}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status
import play.api.http.Status.OK
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.ControllerComponents
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, stubControllerComponents}
import testUtils.TestSupport
import play.api.test.Helpers._


class CalculationControllerSpec extends TestSupport with MockSchemaValidation with MockDataRepository with ScalaFutures {
  lazy val mockCC: ControllerComponents = stubControllerComponents()
  implicit val calcSuccessReponseWrites: OWrites[CalcSuccessReponse] = Json.writes[CalcSuccessReponse]
  object CalcControllerUnderTest extends CalculationController(mockCC, mockDataRepository)

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

  val calcResponseJson = Json.toJson(calcResponse).toString()

  lazy val successWithBodyModel: DataModel = DataModel(
    _id = "test",
    schemaId = "testID2",
    method = "GET",
    status = Status.OK,
    response = Some(Json.parse(calcResponseJson))
  )

  "generateCalculationList" should {
    "expected list of ids" in {
      //mockFind(Some(successWithBodyModel))
      lazy val result = CalcControllerUnderTest.generateCalculationListFor2023_24("1111AAAA")(FakeRequest())

      println(result.futureValue)


      status(result) shouldBe Status.OK
    }

//    "return empty list of ids" in {
//    }
  }

//  "generateCalculationList" should {
//    "return calc details" in {
//    }
//  }

}
