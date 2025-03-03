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

package parsers

import org.scalatest.funsuite.AnyFunSuite

class ITSAStatusUrlParserSpec extends AnyFunSuite {
  import ITSAStatusUrlParser.extractTaxYear

  test("An empty string return None") {
    assert(extractTaxYear("") == None)
  }

  test("return correct tax year / case A: 23-24") {
    val url = "/income-tax/AS000002A/person-itd/itsa-status/23-24?futureYears=false&history=false"
    assert(extractTaxYear(url) == Some("23-24"))
  }

  test("return correct tax year / case B: 20-21") {
    val url = "/income-tax/CE453003A/person-itd/itsa-status/20-21?futureYears=false&history=false"
    assert(extractTaxYear(url) == Some("20-21"))
  }

  test("return correct tax year / case C: 23-24") {
    val url = "income-tax/AS000002A/person-itd/itsa-status/23-24"
    assert(extractTaxYear(url) == Some("23-24"))
  }

}
