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

import testUtils.TestSupport
import utils.CalculationUtils.{createCalSummaryModel, getTaxYearRangeEndYear}

class CalculationUtilsSpec extends TestSupport {
  import CalculationUtils.createCalResponseModel

  val taxYear: Int = 2024

  "call getCalculationListSuccessResponse" should {

    "create calcResponse model: AA888888A" in {
      val calcResponse = createCalResponseModel("AA888888A", Some(taxYear), true).toOption
      calcResponse.isDefined shouldBe true
      calcResponse.get.size shouldBe 1
      calcResponse.get.head.calculationId shouldBe "041f7e4d-87d9-4d4a-a296-3cfbdf2024a8"
      calcResponse.get.head.calculationTimestamp shouldBe "2018-07-13T12:13:48.763Z"
      calcResponse.get.head.calculationType shouldBe "inYear"
      calcResponse.get.head.requestedBy shouldBe "customer"
      calcResponse.get.head.totalIncomeTaxAndNicsDue shouldBe BigDecimal("1250.00")
      calcResponse.get.head.intentToCrystallise shouldBe false
      calcResponse.get.head.crystallised shouldBe true
      calcResponse.get.head.year shouldBe taxYear
    }

    "be always single element" in {
      List("AA888888A", "AY888881A", "AY999991A", "XXYYTTEEA").foreach { nino =>
        val calcResponse = createCalResponseModel(nino, Some(taxYear), true).toOption
        calcResponse.get.size shouldBe 1
      }
    }

    "fail for nino in wrong format" in {
      createCalResponseModel("AAAAYY", Some(taxYear), true) match {
        case Left(error) =>
          error.getMessage should fullyMatch regex """([String\sindex\sout\sof\srange:\s7|Index\s7\sout\sof\sbounds\sfor\slength\s6]+)+"""
        case Right(_) => fail("Failing scenario")
      }
    }

    "getTaxYearRangeEndYear" in {
      val result1 = getTaxYearRangeEndYear("23-24")
      val result2 = getTaxYearRangeEndYear("24-25")
      val result3 = getTaxYearRangeEndYear("25-26")
      val result4 = getTaxYearRangeEndYear("70-71")

      result1 shouldBe 2024
      result2 shouldBe 2025
      result3 shouldBe 2026
      result4 shouldBe 2071
    }
  }

  "call createCalSummaryModel" should {

    "create calcResponse model: AA888888A" in {
      val calcResponse = createCalSummaryModel("AA888888A", taxYear, true).toOption
      calcResponse.isDefined shouldBe true
      calcResponse.get.size shouldBe 1
      calcResponse.get.head.calculationId shouldBe "041f7e4d-87d9-4d4a-a296-3cfbdf2024a8"
      calcResponse.get.head.calculationTimestamp shouldBe "2018-07-13T12:13:48.763Z"
      calcResponse.get.head.calculationType shouldBe "DF"
      calcResponse.get.head.requestedBy shouldBe "CUSTOMER"
    }

    "be always single element" in {
      List("AA888888A", "AY888881A", "AY999991A", "XXYYTTEEA").foreach { nino =>
        val calcResponse = createCalSummaryModel(nino, taxYear, true).toOption
        calcResponse.get.size shouldBe 1
      }
    }

    "fail for nino in wrong format" in {
      createCalSummaryModel("AAAAYY", taxYear, true) match {
        case Left(error) =>
          error.getMessage should fullyMatch regex """([String\sindex\sout\sof\srange:\s7|Index\s7\sout\sof\sbounds\sfor\slength\s6]+)+"""
        case Right(_) => fail("Failing scenario")
      }
    }

    "getTaxYearRangeEndYear" in {
      val result1 = getTaxYearRangeEndYear("23-24")
      val result2 = getTaxYearRangeEndYear("24-25")
      val result3 = getTaxYearRangeEndYear("25-26")
      val result4 = getTaxYearRangeEndYear("70-71")

      result1 shouldBe 2024
      result2 shouldBe 2025
      result3 shouldBe 2026
      result4 shouldBe 2071
    }
  }
}
