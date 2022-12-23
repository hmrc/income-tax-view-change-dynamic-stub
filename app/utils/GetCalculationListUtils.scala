/*
 * Copyright 2022 HM Revenue & Customs
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

object GetCalculationListUtils {

  def ninoMatchCharacters = (nino: String) => s"${nino.charAt(0)}${nino.charAt(7)}"

  def calculationId(lastTwoChars: String, taxYearOpt: Option[Int]): String = {
    taxYearOpt.map(taxYear => {
      s"041f7e4d-87d9-4d4a-a296-3cfbdf${taxYear.toLong.toString}$lastTwoChars"
    }).getOrElse(s"041f7e4d-87d9-4d4a-a296-3cfbdf92f1$lastTwoChars")
  }

  def getCalculationListSuccessResponse(lastTwoChars: String, taxYear: Option[Int], crystallised: Boolean = false): String = {
    s"""
       |[
       |      {
       |        "calculationId" : "${calculationId(lastTwoChars, taxYear)}",
       |        "calculationTimestamp" : "2018-07-13T12:13:48.763Z",
       |        "calculationType" : "inYear",
       |        "requestedBy" : "customer",
       |        "year" : 2019,
       |        "fromDate" : "2018-04-06",
       |        "toDate" : "2019-04-05",
       |        "totalIncomeTaxAndNicsDue" : 1250.00,
       |        "intentToCrystallise" : false,
       |        "crystallised" : $crystallised
       |      }
       |]
       |""".stripMargin
  }
}

