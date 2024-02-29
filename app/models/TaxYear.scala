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

case class TaxYear(endYear: Int) {

  val startYear: Int = endYear - 1
  val endYearString: String = endYear.toString

  def formattedTaxYearRange: String = {
    s"${startYear.toString.takeRight(2)}-${endYear.toString.takeRight(2)}"
  }

  def formattedTaxYearRangeLong: String = {
    s"20${startYear.toString.takeRight(2)}-${endYear.toString.takeRight(2)}"
  }

  def is1896: Boolean = endYear >= 24

}

object TaxYear {

  def createTaxYearGivenTaxYearRange(taxYearRange: String): Option[TaxYear] = {
    val taxYearEndString: String = "20" + taxYearRange.takeRight(2)
    try {
      Some(TaxYear(taxYearEndString.toInt))
    } catch {
      case _: NumberFormatException => None
    }
  }

}
