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

import models.CalcSuccessReponse

import scala.util.Try

object CalculationUtils {

  private def ninoMatchCharacters(nino: String): String =
    s"${nino.charAt(0)}${nino.charAt(7)}"


  private def calculationId(lastTwoChars: String, taxYearOpt: Option[Int]): String = {
    taxYearOpt.map(taxYear => {
      s"041f7e4d-87d9-4d4a-a296-3cfbdf${taxYear.toLong.toString}$lastTwoChars"
    }).getOrElse(s"041f7e4d-87d9-4d4a-a296-3cfbdf92f1$lastTwoChars")
  }

  def createCalResponseModel(nino: String, taxYear: Option[Int], crystallised:
  Boolean = false): Either[Throwable, List[CalcSuccessReponse]] = {
    Try {
      val encodedNino = ninoMatchCharacters(nino)
      List(getCalcResponse(taxYear, crystallised, encodedNino))
    }.toEither
  }

  def getTaxYearRangeEndYear(taxYearRange: String): Int = {
    s"20${taxYearRange.takeRight(2)}".toInt
  }

  private def getCalcResponse(taxYear: Option[Int],
                              crystallised: Boolean, calcEncoding: String): CalcSuccessReponse = {
    CalcSuccessReponse(
      calculationId = s"${calculationId(calcEncoding, taxYear).toLowerCase()}",
      calculationTimestamp = "2018-07-13T12:13:48.763Z",
      calculationType = "inYear",
      requestedBy = "customer",
      year = taxYear.getOrElse(2018),
      fromDate = "2018-04-06",
      toDate = "2019-04-05",
      totalIncomeTaxAndNicsDue = BigDecimal("1250.00"),
      intentToCrystallise = false,
      crystallised = crystallised
    )
  }

  def getFallbackUrlLegacy(calcId: String): String = {
    val taxYear = calcId.substring(calcId.length - 6, calcId.length - 2)
    s"/income-tax/view/calculations/liability/SUCCESS1A/041f7e4d-87d9-4d4a-a296-3cfbdf${taxYear}s1"
  }

  def getFallbackUrlTYS(taxYearRange: String): String = {
    val taxYear = s"20${taxYearRange.takeRight(2)}"
    s"/income-tax/view/calculations/liability/$taxYearRange/SUCCESS1A/041f7e4d-87d9-4d4a-a296-3cfbdf${taxYear}s1"
  }
}
