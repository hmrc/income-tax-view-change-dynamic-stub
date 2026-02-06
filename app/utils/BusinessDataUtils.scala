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

import org.mongodb.scala.Document

import java.time.LocalDate

object BusinessDataUtils {

  final val businessDataKey = "response.success.taxPayerDisplayResponse.businessData"
  final val propertyDataKey = "response.success.taxPayerDisplayResponse.propertyData"
  private val currentYear: Int = LocalDate.now().getYear

  def createBusinessData(activeSoleTrader: Boolean, ceasedSoleTrader: Boolean): Seq[Document] = {

    def businessDocument(isCeased: Boolean, index: Int): Document = {
      val cessation: Option[(String, String)] = if (isCeased) Some("cessationDate" -> s"${currentYear - 1}-06-30") else None
      val base = Document(
        "incomeSourceId" -> s"XAIS0000000000$index",
        "accPeriodSDate" -> s"$currentYear-04-01",
        "accPeriodEDate" -> s"${currentYear + 1}-03-31",
        "tradingName" -> s"Business $index",
        "incomeSource" -> s"Manufacturing $index",
        "businessAddressDetails" -> Document(
          "addressLine1" -> s"$index Street Street",
          "addressLine2" -> "Cityburg",
          "addressLine3" -> "Countryshire",
          "addressLine4" -> "Townville",
          "postalCode" -> s"AA$index AAA",
          "countryCode" -> "GB"
        ),
        "tradingSDate" -> "2013-01-01",
        "seasonalFlag" -> false,
        "paperLessFlag" -> true,
        "firstAccountingPeriodStartDate" -> "2017-04-01",
        "firstAccountingPeriodEndDate" -> "2018-03-31"
      )

      cessation.fold(base) { kv => base ++ Document(kv)
      }
    }

    (activeSoleTrader, ceasedSoleTrader) match {
      case (false, false) => Seq.empty
      case (true, false) => Seq(businessDocument(isCeased = false, index = 1))
      case (false, true) => Seq(businessDocument(isCeased = true, index = 2))
      case (true, true) => Seq(businessDocument(isCeased = false, index = 1), businessDocument(isCeased = true, index = 2))
    }
  }

  def createPropertyData(ukProperty: Boolean, foreignProperty: Boolean): Seq[Document] = {

    def propertyDocument(incomeSourceType: String, incomeSourceId: String): Document =
      Document(
        "incomeSourceId"                  -> incomeSourceId,
        "accPeriodSDate"                  -> s"$currentYear-04-06",
        "accPeriodEDate"                  -> s"${currentYear + 1}-04-05",
        "numPropRentedUK"                 -> "4",
        "numPropRentedEEA"                -> "0",
        "numPropRentedNONEEA"             -> "0",
        "numPropRented"                   -> "4",
        "paperLessFlag"                   -> true,
        "firstAccountingPeriodStartDate"  -> "2017-04-06",
        "firstAccountingPeriodEndDate"    -> "2018-04-05",
        "incomeSourceType"                -> incomeSourceType,
        "tradingSDate"                    -> "2015-05-01"
      )

      Seq(
        Option.when(ukProperty)(propertyDocument("02", "XAIS00000000011")),
        Option.when(foreignProperty)(propertyDocument("03", "XAIS00000000012"))
      ).flatten

  }
}
