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
import org.mongodb.scala.Document

import java.time.{LocalDate, LocalDateTime, ZoneOffset}
import java.time.format.DateTimeFormatter

object CustomUserUtils {

  private val currentYear: Int = LocalDate.now().getYear

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

  def createBusinessDetailsArray(numberOfSoleTraders: Int, isCeased: Boolean): Seq[Document] = {
    val cessation: Option[(String, String)] = if (isCeased) Some("cessationDate" -> s"${currentYear - 1}-06-30") else None

    (1 to numberOfSoleTraders).map { index =>
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

      cessation.fold(base) { kv => base ++ Document(kv) }
    }
  }


  def createPropertyData(
                          ukProperty: Boolean,
                          foreignProperty: Boolean,
                          numberOfCeasedUk: Int,
                          numberOfCeasedForeign: Int
                        ): Seq[Document] = {

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

    def ceased(doc: Document): Document =
      doc ++ Document("cessationDate" -> s"${currentYear - 1}-06-30")

    val active: Seq[Document] =
      Seq(
        Option.when(ukProperty)(propertyDocument("02", "XAIS00000000011")),
        Option.when(foreignProperty)(propertyDocument("03", "XAIS00000000012"))
      ).flatten

    val ceasedUk: Seq[Document] =
      (1 to numberOfCeasedUk).map { i =>
        ceased(propertyDocument("02", s"XAIS0000000002$i"))
      }

    val ceasedForeign: Seq[Document] =
      (1 to numberOfCeasedForeign).map { i =>
        ceased(propertyDocument("03", s"XAIS0000000003$i"))
      }

    active ++ ceasedUk ++ ceasedForeign
  }

  def createCalculationData(latestCalculationType: String, previousCalculationType: String): Seq[Document] = {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.S'Z'")
    val latestTimeStamp = LocalDateTime.now().atOffset(ZoneOffset.UTC).format(formatter)
    val previousTimeStamp = LocalDateTime.now().minusDays(1).atOffset(ZoneOffset.UTC).format(formatter)

    def calculationSummaryDoc(calcType: String, id: String, timestamp: String) = {
      Document(
        "calculationId" -> s"1d35cfe4-cd23-22b2-b074-fae6052025b$id",
        "calculationTimestamp" -> s"$timestamp",
        "calculationType" -> s"$calcType",
        "requestedBy" -> "CUSTOMER",
        "calculationTrigger" -> "class2NICEvent",
        "calculationOutcome" -> "PROCESSED"
      )
    }
    val latestCalculationDoc = if (latestCalculationType.nonEmpty) { calculationSummaryDoc(latestCalculationType, "1", latestTimeStamp) } else Document()

    val previousCalculationDoc = if (previousCalculationType.nonEmpty) { calculationSummaryDoc(previousCalculationType, "2", previousTimeStamp) } else Document()

    Seq(latestCalculationDoc, previousCalculationDoc).filter(_.nonEmpty)
  }

  private def convertCalculationType(calculationType: String): String = {
    calculationType match {
      case "In Year" => "IY"
      case "Intent to Finalise" => "IF"
      case "Intent to Amend" => "IA"
      case "Declare Finalisation" => "DF"
      case "Confirm Amendment" => "CA"
      case "Correction" => "CO"
      case "No Calculation" => ""
    }
  }
}