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

import com.typesafe.config.Config
import mocks.{MockDataRepository, MockSchemaValidation}
import models.DataModel
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.http.Status
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.Result
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testUtils.TestSupport

import scala.concurrent.Future

class SetupDataControllerSpec extends TestSupport with MockSchemaValidation with MockDataRepository {

  class Setup(configResult: Boolean = true) {

    val mockConfig: Config = mock[Config]
    when(mockConfig.getBoolean(any())).thenReturn(configResult)

    val mockCC = stubMessagesControllerComponents()

    val controller = new SetupDataController(
      mockSchemaValidation,
      mockDataRepository,
      mockCC,
      mockConfig
    )
  }

  "SetupDataController.addData" when {

    "validateUrlMatch returns 'true'" should {

      val model: DataModel = DataModel(
        _id = "1234",
        schemaId = "2345",
        method = "GET",
        response = Some(Json.parse("{}")),
        status = Status.OK
      )

      "when validateResponseJson returns 'true'" should {

        "return Status OK (200) if data successfully added to stub" in new Setup {
          lazy val request: FakeRequest[JsValue] =
            FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
          lazy val result: Future[Result] = controller.addData(request)

          mockValidateUrlMatch("2345", "1234")(response = true)
          mockValidateResponseJson("2345", Some(Json.parse("{}")))(response = true)
          mockAddEntry(model)(successUpdateResult)
          status(result) shouldBe Status.OK
        }
        "return Status InternalServerError (500) if unable to add data to the stub" in new Setup {
          lazy val request =
            FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
          lazy val result = controller.addData(request)

          mockValidateUrlMatch("2345", "1234")(response = true)
          mockValidateResponseJson("2345", Some(Json.parse("{}")))(response = true)
          mockAddEntry(model)(failedUpdateResult)
          status(result) shouldBe Status.INTERNAL_SERVER_ERROR
        }

      }

      "return Status BadRequest (400) when validateResponseJson returns 'false'" in new Setup {
        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result  = controller.addData(request)

        mockValidateUrlMatch("2345", "1234")(response = true)
        mockValidateResponseJson("2345", Some(Json.parse("""{}""")))(response = false)
        status(result) shouldBe Status.BAD_REQUEST
      }

      "return Status when validateResponseJson returns 'false'" in new Setup(false) {
        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result  = controller.addData(request)

        mockValidateUrlMatch("2345", "1234")(response = true)
        mockValidateResponseJson("2345", Some(Json.parse("""{}""")))(response = true)
        mockAddEntry(model)(successUpdateResult)
        status(result) shouldBe Status.OK
      }

      "return Status InternalServerError (500) when validateResponseJson returns 'false'" in new Setup {
        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result  = controller.addData(request)

        mockValidateUrlMatch("2345", "1234")(response = true)
        mockValidateResponseJson("2346", Some(Json.parse("""{}""")))(response = false)
        status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      }
    }

    "validateUrlMatch returns 'false'" should {

      val model: DataModel = DataModel(
        _id = "1234",
        schemaId = "2345",
        method = "GET",
        response = Some(Json.parse("{}")),
        status = Status.OK
      )

      "return Status BadRequest (400)" in new Setup {
        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result  = controller.addData(request)

        mockValidateUrlMatch("2345", "1234")(response = false)
        mockLoadUrlRegex("2345")(response = "w")
        status(result) shouldBe Status.BAD_REQUEST
      }

    }

    "not a GET request" should {

      val model: DataModel = DataModel(
        _id = "1234",
        schemaId = "2345",
        method = "BLOB",
        response = Some(Json.parse("{}")),
        status = Status.OK
      )

      "return Status BadRequest (400)" in new Setup {
        lazy val request = FakeRequest().withBody(Json.toJson(model)).withHeaders(("Content-Type", "application/json"))
        lazy val result  = controller.addData(request)

        status(result) shouldBe Status.BAD_REQUEST
      }

    }

  }

  "SetupDataController.removeData" should {

    "return Status OK (200) on successful removal of data from the stub" in new Setup {
      lazy val request = FakeRequest()
      lazy val result  = controller.removeData("someUrl")(request)

      mockRemoveById("someUrl")(successDeleteResult)

      status(result) shouldBe Status.OK
    }

    "return Status InternalServerError (500) on unsuccessful removal of data from the stub" in new Setup {
      lazy val request = FakeRequest()
      lazy val result  = controller.removeData("someUrl")(request)

      mockRemoveById("someUrl")(failedDeleteResult)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

  }

  "SetupDataController.removeAllData" should {

    "return Status OK (200) on successful removal of all stubbed data" in new Setup {
      lazy val request = FakeRequest()
      lazy val result  = controller.removeAll()(request)

      mockRemoveAll()(successDeleteResult)

      status(result) shouldBe Status.OK
    }

    "return Status InternalServerError (500) on successful removal of all stubbed data" in new Setup {
      lazy val request = FakeRequest()
      lazy val result  = controller.removeAll()(request)

      mockRemoveAll()(failedDeleteResult)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
    }

  }
}
