/*
 * Copyright 2023 HM Revenue & Customs
 *
 */

package utils

object GetCalculationListUtils {

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
