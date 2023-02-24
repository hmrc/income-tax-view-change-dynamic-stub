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

import play.api.libs.functional.syntax._
import play.api.libs.json._

case class KVPair(key: String, value: String)
case class Enrolment(key: String, identifiers: Seq[KVPair], state: String)
case class EnrolmentValues(mtditid: String, utr: String)
object LoginUtil {

  implicit val kvPairWrites: Writes[KVPair] = (
    (JsPath \ "key").write[String] and
      (JsPath \ "value").write[String]
    )(unlift(KVPair.unapply))

  implicit val enrolmentWrites: Writes[Enrolment] = (
    (JsPath \ "key").write[String] and
      (JsPath \ "identifiers").write[Seq[KVPair]] and
      (JsPath \ "state").write[String]
    )(unlift(Enrolment.unapply))


  val reDirectURL = "http://localhost:9081/report-quarterly/income-and-expenses/view?origin=BTA"

  val enrolmentData: EnrolmentValues => JsValue = (enrolment: EnrolmentValues) => {
    val es =
      Seq(
        Enrolment(key = "HMRC-MTD-IT", identifiers =
          Seq(KVPair(key = "MTDITID", value = enrolment.mtditid)), state = "Activated"),
        Enrolment(key = "IR-SA", identifiers =
          Seq(KVPair(key = "UTR", value = enrolment.utr)), state = "Activated")
      )
    Json.toJson[Seq[Enrolment]](es)
  }



}
