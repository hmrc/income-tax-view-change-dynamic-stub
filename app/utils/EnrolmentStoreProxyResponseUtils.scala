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

package utils

import play.api.libs.json.{JsObject, Json}

object EnrolmentStoreProxyResponse {

  def generateResponse: JsObject = {
    Json.obj(
      "startRecord" -> 1,
      "enrolments" -> Json.arr(
        Json.obj(
          "service" -> "HMRC-MTD-IT",
          "friendlyName" -> "Clever+Digs+Set+2",
          "state" -> "Activated",
          "identifiers" -> Json.arr(
            Json.obj("key" -> "MTDITID", "value" -> "XAIT00000111111")
          ),
          "enrolmentDate" -> "2024-07-04T14:15:15.078",
          "activationDate" -> "2024-07-04T14:15:15.078"
        ),
        Json.obj(
          "service" -> "HMRC-MTD-IT",
          "friendlyName" -> "Damsels+In+This+Dress",
          "state" -> "Activated",
          "identifiers" -> Json.arr(
            Json.obj("key" -> "MTDITID", "value" -> "XDIT22222222222")
          ),
          "enrolmentDate" -> "2024-07-04T14:21:08.203",
          "activationDate" -> "2024-07-04T14:21:08.203"
        ),
        Json.obj(
          "service" -> "HMRC-MTD-IT",
          "friendlyName" -> "Clever+Digs+Set+5",
          "state" -> "Activated",
          "identifiers" -> Json.arr(
            Json.obj("key" -> "MTDITID", "value" -> "XYIT77777777777")
          ),
          "enrolmentDate" -> "2024-07-04T14:22:25.981",
          "activationDate" -> "2024-07-04T14:22:25.981"
        ),
        Json.obj(
          "service" -> "HMRC-MTD-IT",
          "friendlyName" -> "Clever+Digs+Set+6",
          "state" -> "Activated",
          "identifiers" -> Json.arr(
            Json.obj("key" -> "MTDITID", "value" -> "XBIT00994530029")
          ),
          "enrolmentDate" -> "2024-07-04T14:23:44.674",
          "activationDate" -> "2024-07-04T14:23:44.674"
        )
      )
    )
  }
}
