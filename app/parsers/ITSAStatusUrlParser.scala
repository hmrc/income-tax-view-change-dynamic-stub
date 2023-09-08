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

object ITSAStatusUrlParser {

  import scala.util.matching.Regex

  private val taxYearExtractor: Regex = """^\/income-tax\/([A-Z0-9]+){9}\/person-itd\/itsa-status/([\d\-]{5})+\?futureYears\=false\&history\=false$""".r

  def extractTaxYear(url: String): Option[String] = url match {
    case taxYearExtractor(_, taxYear) => Some(taxYear)
    case _ => None
  }

}
