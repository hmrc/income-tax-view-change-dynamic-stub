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

package models

import play.api.data.Forms.{mapping, optional, text}
import play.api.data.Mapping
import play.api.libs.json.{Json, OWrites}

trait ItmpFormData {
  protected def itmpMapping(): Mapping[Option[ItmpData]] = {
    optional(
      mapping(
        "givenName"   -> optional(text),
        "middleName"  -> optional(text),
        "familyName"  -> optional(text),
        "dateOfBirth" -> optional(text),
        "address" -> mapping(
          "line1"       -> optional(text),
          "line2"       -> optional(text),
          "line3"       -> optional(text),
          "line4"       -> optional(text),
          "line5"       -> optional(text),
          "postCode"    -> optional(text),
          "countryCode" -> optional(text),
          "countryName" -> optional(text)
        )(ItmpAddress.apply)(ItmpAddress.unapply)
      )(ItmpData.apply)(ItmpData.unapply)
    )
  }
}

case class ItmpData(
    givenName:  Option[String],
    middleName: Option[String],
    familyName: Option[String],
    birthdate:  Option[String],
    address:    ItmpAddress)

object ItmpData {
  implicit val writes: OWrites[ItmpData] = Json.writes[ItmpData]
}

case class ItmpAddress(
    line1:       Option[String],
    line2:       Option[String],
    line3:       Option[String],
    line4:       Option[String],
    line5:       Option[String],
    postCode:    Option[String],
    countryCode: Option[String],
    countryName: Option[String])

object ItmpAddress {
  implicit val writes: OWrites[ItmpAddress] = Json.writes[ItmpAddress]
}
