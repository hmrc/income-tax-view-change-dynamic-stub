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

package repositories

import models.DataModel
import models.HttpMethod.GET
import org.mongodb.scala.model.Filters.equal
import parsers.ITSAStatusUrlParser.extractTaxYear
import play.api.Logging
import play.api.libs.json.{JsValue, Json, OWrites}
import play.api.mvc.{AnyContent, MessagesControllerComponents, MessagesRequest, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DefaultValues @Inject()(dataRepository: DataRepository,
                              cc: MessagesControllerComponents)
                             (implicit val ec: ExecutionContext) extends FrontendController(cc) with Logging {
  case class ITSAStatus(taxYear: String, itsaStatusDetails: List[ITSAStatusDetails])

  case class ITSAStatusDetails(submittedOn: String, status: String, statusReason: String)

  implicit val ITSAITSAStatusDetailsWriter: OWrites[ITSAStatusDetails] = Json.writes[ITSAStatusDetails]
  implicit val ITSAStatusWriter: OWrites[ITSAStatus] = Json.writes[ITSAStatus]

  private def getItsaStatusDefaultJson(taxYear: String): JsValue = {
    val itsaAStatus = List(ITSAStatus(taxYear = taxYear, itsaStatusDetails = List(
      ITSAStatusDetails(submittedOn = "2022-01-10T06:14:00Z", status = "MTD Voluntary",
        statusReason = "Sign up - return available"))))
    Json.toJson(itsaAStatus)
  }

  def getResponse(url: String)(implicit request: MessagesRequest[AnyContent]): Result = {
    extractTaxYear(url) match {
      case Some(taxYear) =>
        val taxYearNormalised = s"20$taxYear" // conversion to format 2023-24
        val json = getItsaStatusDefaultJson(taxYearNormalised)
        logger.info(s"DefaultValues applied: $json - for: $url")
        Status(OK)(json)
      case None =>
        NotFound(s"Could not find endpoint in Dynamic Stub matching the URI: ${request.uri}")
    }
  }

  def getDefaultRequestHandler(url: String): Future[Result] = {
    dataRepository
      .find(equal("_id", url), equal("method", GET))
      .map {
        case stubData@Some(_: DataModel) =>
          Status(stubData.head.status)(stubData.head.response.get)
        case None =>
          NotFound(s"Failed to find the default API endpoint in the repository matching the URI: $url")
      }
  }
}
