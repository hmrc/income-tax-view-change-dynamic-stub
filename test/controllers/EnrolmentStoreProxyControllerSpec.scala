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

import org.scalatest.concurrent.ScalaFutures
import play.api.http.Status.{INTERNAL_SERVER_ERROR, OK}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import testUtils.TestSupport
import utils.EnrolmentStoreProxyResponse

class EnrolmentStoreProxyControllerSpec extends TestSupport with ScalaFutures {

  object TestEnrolmentStoreProxyController extends EnrolmentStoreProxyController(stubMessagesControllerComponents())

  "getUTRList" should {

    "return status OK with the expected JSON response" in {
      val groupId      = "testGroupId"
      val expectedJson = EnrolmentStoreProxyResponse.generateResponse
      val result       = TestEnrolmentStoreProxyController.getUTRList(groupId)(FakeRequest())
      status(result) shouldBe OK
      contentAsJson(result) shouldBe expectedJson
    }

    "return status InternalServerError when an invalid groupId is provided" in {
      val groupId = ""
      val result  = TestEnrolmentStoreProxyController.getUTRList(groupId)(FakeRequest())
      status(result) shouldBe INTERNAL_SERVER_ERROR
    }
  }
}
