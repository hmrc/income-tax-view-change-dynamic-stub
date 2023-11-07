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
import play.api.test.Helpers._
import repositories.DefaultValues
import testUtils.TestSupport


class CalculationControllerSpec extends TestSupport with MockSchemaValidation with MockDataRepository with ScalaFutures with DataHelper {

  val defaultValues: DefaultValues = app.injector.instanceOf[DefaultValues]
  object TestRequestHandlerController extends RequestHandlerController(mockSchemaValidation,
    mockDataRepository,
    mockCC,
    defaultValues)

  object CalcControllerUnderTest extends CalculationController(mockCC, mockDataRepository, TestRequestHandlerController, app.configuration, defaultValues)

  "generateCalculationListTYS" should {

    "return status OK" in {
      val result = CalcControllerUnderTest.generateCalculationListTYS(ninoTest, "23-24")(FakeRequest())
      status(result) shouldBe OK
    }
    "return status OK future taxYear" in {
      val result = CalcControllerUnderTest.generateCalculationListTYS(ninoTest, "25-26")(FakeRequest())
      status(result) shouldBe OK
    }

    "return status BadRequest when internal error" in {
      val result = CalcControllerUnderTest.generateCalculationListTYS(ninoWithWrongFormat, "23-24")(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }
  }

  "getCalculationDetailsTYS" should {
    "data for given Nino and TaxYear exists: return OK" in {
      mockFind(Some(successWithBodyModel))
      val result1 = CalcControllerUnderTest
        .getCalculationDetailsTYS(nino = ninoTest, calculationId = calculationIdTest, "23-24")(FakeRequest())
      val result2 = CalcControllerUnderTest
        .getCalculationDetailsTYS(nino = ninoTest, calculationId = calculationIdTest, "24-25")(FakeRequest())
      status(result1) shouldBe OK
      status(result2) shouldBe OK
    }

    "no data for given Nino and TaxYear: return NotFound" in {
      mockFind(None)
      val result = CalcControllerUnderTest
        .getCalculationDetailsTYS(nino = "", calculationId = calculationIdTest, "23-24")(FakeRequest())
      status(result) shouldBe BAD_REQUEST
    }

    "no data for given Nino and TaxYear: empty response" in {
      mockFind(Some(successWithEmptyBody))
      val result = CalcControllerUnderTest
        .getCalculationDetailsTYS(nino = ninoTest, calculationId = calculationIdTest, "23-24")(FakeRequest())
      status(result) shouldBe NOT_FOUND
    }
  }

}
