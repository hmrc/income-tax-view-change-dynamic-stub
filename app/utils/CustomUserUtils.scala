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

package utils

import models.CustomUserModel

object CustomUserUtils {

  def convertCustomUserFields(customUser: CustomUserModel): CustomUserModel = {
    val convertedChannel = customUser.channel match {
      case "Customer Led" => "1"
      case "HMRC Unconfirmed" => "2"
      case "HMRC Confirmed" => "3"
    }

    customUser.copy(
      channel = convertedChannel,
      calculationTypeLatest = convertCalculationType(customUser.calculationTypeLatest),
      calculationTypePrevious = convertCalculationType(customUser.calculationTypePrevious)
    )
  }

  private def convertCalculationType(calculationType: String): String = {
    calculationType match {
      case "In Year" => "IY"
      case "Intent to Finalise" => "IF"
      case "Intent to Amend" => "IA"
      case "Declare Finalisation" => "DF"
      case "Confirm Amendment" => "CA"
      case "Correction" => "CO"
      case "None" => ""
    }
  }
}