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

import mocks.{MockDataRepository, MockSchemaValidation}
import models.{DataModel, SchemaModel}
import play.api.http.Status
import play.api.libs.json.Json
import play.api.mvc.{ControllerComponents, MessagesControllerComponents}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testUtils.TestSupport

class RequestHandlerControllerSpec extends TestSupport with MockSchemaValidation with MockDataRepository {
  lazy val mockCC: MessagesControllerComponents = stubMessagesControllerComponents()

  object TestRequestHandlerController extends RequestHandlerController(mockSchemaValidation,
    mockDataRepository,
    mockCC)

  lazy val successModel: DataModel = DataModel(
    _id = "test",
    schemaId = "testID1",
    method = "GET",
    status = Status.OK,
    response = None
  )

  lazy val successWithBodyModel: DataModel = DataModel(
    _id = "test",
    schemaId = "testID2",
    method = "GET",
    status = Status.OK,
    response = Some(Json.parse("""{"Something" : "hello"}"""))
  )

  lazy val successRequestSchema: SchemaModel = SchemaModel(
    _id = "testRequest",
    url = "SomeURL",
    method = "POST",
    responseSchema = Json.parse("""{"response" : "sup"}"""),
    requestSchema = Some(Json.parse("""{"request" : "jaffa cakes"}"""))
  )

  "The getRequestHandler method" should {

    "return the status code specified in the model" in {
      lazy val result = TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())

      mockFind(Some(successModel))
      status(result) shouldBe Status.OK
    }

    "return the status and body" in {
      lazy val result = TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())

      mockFind(Some(successWithBodyModel))
      status(result) shouldBe Status.OK
      contentAsJson(result) shouldBe successWithBodyModel.response.get
    }

    "return a 404 status when the endpoint cannot be found" in {
      lazy val result = TestRequestHandlerController.getRequestHandler("/test")(FakeRequest())

      mockFind(None)
      status(result) shouldBe Status.NOT_FOUND
    }
  }

  "The postRequestHandler method" should {

    "return the corresponding response of an incoming POST request" in {
      lazy val result = TestRequestHandlerController.postRequestHandler("/test")(FakeRequest())

      mockFind(Some(successWithBodyModel))
      mockValidateRequestJson(successWithBodyModel.schemaId, successRequestSchema.requestSchema)(response = true)

      contentAsJson(result) shouldBe successWithBodyModel.response.get
    }

    "return a response status when there is no stubbed response body for an incoming POST request" in {
      lazy val result = TestRequestHandlerController.postRequestHandler("/test")(FakeRequest())

      mockFind(Some(successModel))
      mockValidateRequestJson(successModel.schemaId, successRequestSchema.requestSchema)(response = true)

      status(result) shouldBe Status.OK
    }

    "return a 400 status if the request body doesn't validate against the stub" in {
      lazy val result = TestRequestHandlerController.postRequestHandler("/test")(FakeRequest())

      mockFind(Some(successWithBodyModel))
      mockValidateRequestJson(successWithBodyModel.schemaId, None)(response = false)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsString(result) shouldBe s"The Json Body:\n\nNone did not validate against the Schema Definition"
    }

    "return a 404 status if the endpoint specified in the POST request can't be found" in {
      lazy val result = TestRequestHandlerController.postRequestHandler("/test")(FakeRequest())

      mockFind(None)

      status(result) shouldBe Status.NOT_FOUND
      contentAsString(result) shouldBe s"Could not find endpoint in Dynamic Stub matching the URI: /"
    }
  }

}
