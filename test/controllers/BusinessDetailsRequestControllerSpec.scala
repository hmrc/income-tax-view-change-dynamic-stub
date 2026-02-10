/*
 * Copyright 2026 HM Revenue & Customs
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

import controllers.helpers.DataHelper
import mocks.MockDataRepository
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status.{BAD_REQUEST, OK}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers.{defaultAwaitTimeout, status}
import testUtils.TestSupport

import scala.concurrent.Future

class BusinessDetailsRequestControllerSpec extends TestSupport with DataHelper with MockDataRepository {

  val mockrequestHandlerController = mock[RequestHandlerController]

  val TestBusinessDetailsRequestController = new BusinessDetailsRequestController(mockCC, mockrequestHandlerController, mockDataRepository)

  val mtdId = "1234567890"

  val successModel = Json.parse(
    """
      |{
      |   "activeSoleTrader": true,
      |   "activeUkProperty": true,
      |   "activeForeignProperty": true,
      |   "ceasedBusiness": false
      |}""".stripMargin)

  "overwriteBusinessData" should {
    "return status OK" when {
      "overwriting business data for a given MTD ID" in {
        lazy val request = FakeRequest().withJsonBody(successModel).withHeaders(("Content-Type", "application/json"))
        when(mockDataRepository.clearAndReplace(any(), any(), any())).thenReturn(Future.successful(successUpdateResult))

        val result = TestBusinessDetailsRequestController.overwriteBusinessData(mtdId)(request)

        status(result) shouldBe OK
      }
    }

    "return status Bad Request" when {
      "given invalid JSON data" in {
        lazy val request = FakeRequest().withJsonBody(Json.parse("""{"invalid": "data"}""")).withHeaders(("Content-Type", "application/json"))

        val result = TestBusinessDetailsRequestController.overwriteBusinessData(mtdId)(request)

        status(result) shouldBe BAD_REQUEST
      }
      "not given JSON data" in {
        lazy val request = FakeRequest().withHeaders(("Content-Type", "application/json"))

        val result = TestBusinessDetailsRequestController.overwriteBusinessData(mtdId)(request)

        status(result) shouldBe BAD_REQUEST
      }
    }
  }
}
