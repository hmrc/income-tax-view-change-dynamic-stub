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

import testUtils.TestSupport

class CalculationUtilsSpec extends  TestSupport{
  import CalculationUtils.createCalResponseModel

  val taxYear : Int = 2024

  "call getCalculationListSuccessResponse" should {

    "create calcResponse model" in {
      val calcResponse = createCalResponseModel("AAAA11YY", Some(taxYear), true).toOption
      calcResponse.isDefined shouldBe true
      calcResponse.get.size shouldBe 1
      calcResponse.get.head.calculationId shouldBe "041f7e4d-87d9-4d4a-a296-3cfbdf2024ay"
      calcResponse.get.head.calculationTimestamp shouldBe "2018-07-13T12:13:48.763Z"
      calcResponse.get.head.calculationType shouldBe "inYear"
      calcResponse.get.head.requestedBy shouldBe "customer"
      calcResponse.get.head.totalIncomeTaxAndNicsDue shouldBe BigDecimal("1250.00")
      calcResponse.get.head.intentToCrystallise shouldBe false
      calcResponse.get.head.crystallised shouldBe true
      calcResponse.get.head.year shouldBe taxYear
    }

    "fail when nino too small" in {
      val Left(error) = createCalResponseModel("AAAAYY", Some(taxYear), true)
      error.isInstanceOf[Throwable] shouldBe true
      error.getMessage shouldBe "String index out of range: 7"
    }

  }
}
