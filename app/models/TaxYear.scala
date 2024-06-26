/*
 * Copyright 2024 HM Revenue & Customs
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

import java.time.LocalDate

case class TaxYear(endYear: Int) {


  private val April = 4
  private val Sixths = 6
  private val Fifth = 5
  val startYear: Int = endYear - 1
  val endYearString: String = endYear.toString

  def toFinancialYearStart: LocalDate = {
    LocalDate.of(this.startYear, April, Sixths)
  }

  def toFinancialYearEnd: LocalDate = {
    LocalDate.of(this.endYear, April, Fifth)
  }

  def rangeShort: String = {
    s"${startYear.toString.takeRight(2)}-${endYear.toString.takeRight(2)}"
  }

  def rangeLong: String = {
    s"20${startYear.toString.takeRight(2)}-${endYear.toString.takeRight(2)}"
  }

}

object TaxYear {

  def createTaxYearGivenTaxYearRange(taxYearRange: String): Option[TaxYear] = {
    val taxYearEndString: String = taxYearRange.takeRight(2)
    try {
      // Add 2,000 to convert it to long year. Eg: 23 -> 2023
      Some(TaxYear(taxYearEndString.toInt + 2000))
    } catch {
      case _: NumberFormatException =>
        None
    }
  }

  def getStartYear(localDate: LocalDate): Int = {
    val taxYear = TaxYear(localDate.getYear)
    if(taxYear.toFinancialYearEnd.isAfter(localDate))
      taxYear.toFinancialYearEnd.getYear
    else
      taxYear.toFinancialYearStart.getYear
  }

}
