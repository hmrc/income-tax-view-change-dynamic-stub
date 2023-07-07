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

package controllers

import controllers.helpers.DataHelper
import mocks.{MockDataRepository, MockSchemaValidation}
import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.OK
import play.api.test.FakeRequest
import play.api.test.Helpers.status
import testUtils.TestSupport
import play.api.test.Helpers._


class CalculationControllerSpec extends TestSupport with MockSchemaValidation with MockDataRepository with ScalaFutures with DataHelper {
  object TestRequestHandlerController extends RequestHandlerController(mockSchemaValidation,
    mockDataRepository,
    mockCC)
  object CalcControllerUnderTest extends CalculationController(mockCC, mockDataRepository, TestRequestHandlerController)

  "generateCalculationListFor2023_24" should {

    "return status OK" in {
      val result = CalcControllerUnderTest.generateCalculationListFor2023_24(ninoTest)(FakeRequest())
      status(result) shouldBe OK
    }

    "return status BadRequest when internal error" in {
      val result = CalcControllerUnderTest.generateCalculationListFor2023_24(ninoWithWrongFormat)(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }
  }

  "getCalculationDetailsFor2023_24" should {
    "data for given Nino and TaxYear exists: return OK" in {
      mockFind(Some(successWithBodyModel))
      val result = CalcControllerUnderTest
        .getCalculationDetailsFor2023_24(nino = ninoTest, calculationId = calculationIdTest)(FakeRequest())
      status(result) shouldBe OK
    }

    "no data for given Nino and TaxYear: return NotFound" in {
      mockFind(None)
      val result = CalcControllerUnderTest
        .getCalculationDetailsFor2023_24(nino = "", calculationId = calculationIdTest)(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }

    "no data for given Nino and TaxYear: empty response" in {
      mockFind(Some(successWithEmptyBody))
      val result = CalcControllerUnderTest
        .getCalculationDetailsFor2023_24(nino = ninoTest, calculationId = calculationIdTest)(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }
  }

}
