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

package controllers

import models.{DataModel, TaxYear}
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.result
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger, Logging}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.PoaUtils

import java.net.URI
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmitPoaController @Inject() (
    cc:                       MessagesControllerComponents,
    requestHandlerController: RequestHandlerController,
    configuration:            Configuration,
    dataRepository:           DataRepository
  )(
    implicit val ec: ExecutionContext)
    extends FrontendController(cc)
    with Logging
    with PoaUtils {

  private val error1773Ninos: Seq[String] = configuration
    .getOptional[Seq[String]]("api1773ErrorResponseNinos")
    .getOrElse(Seq.empty)

  def interceptPoaSubmit(): Action[AnyContent] =
    Action.async { implicit request =>
      val bodyOption: Option[JsValue] = request.body.asJson
      val InvalidJsonError = BadRequest("Invalid JSON in the UpdateIncomeSourceRequest body")
      val NoNinoError      = BadRequest("Nino not found in the ClaimToAdjustPoaRequest body")

      bodyOption match {
        case Some(body) =>
          val ninoOpt = extractNino(body)
          ninoOpt match {
            case Some(nino) =>
              val newRequest =
                if (error1773Ninos.contains(nino.toUpperCase)) {
                  // Retrieve stubbed error response from ATs
                  Logger("application").info(s"Returning stubbed API#1773 error response for nino: $nino")
                  request.withTarget(request.target.withUri(URI.create(request.uri + s"?nino=$nino")))
                } else {
                  //Replace poa amount with new amount
                  overwriteTotalAmount(nino, body)
                  // Retrieve stubbed success response from ATs
                  Logger("application").info(s"Returning stubbed API#1773 success response for nino: $nino")
                  request
                }
              requestHandlerController.postRequestHandler(newRequest.uri).apply(newRequest)
            case None => Future.successful(NoNinoError)
          }
        case None => Future.successful(InvalidJsonError)
      }
    }

  private def overwriteTotalAmount(nino: String, json: JsValue): Unit = {
    (extractPoAAmount(json), extractTaxYear(json)) match {
      case (Some(amount), Some(taxYearString)) =>
        TaxYear.createTaxYearGivenTaxYearRange(taxYearString) match {
          case Some(taxYear) =>
            val financialUrl             = getFinancialDetailsUrl(nino, taxYear)
            val financialDetailsResponse = dataRepository.find(equal("_id", financialUrl))
            transformFinancialDetailsResponse(amount, financialUrl, nino, financialDetailsResponse)
          case None =>
            Logger("application").info(s"=>>Failed to create tax year from request")
            Future.failed(new Exception("Failed to create tax year from request"))
        }
      case _ => Future.failed(new Exception("Could not extract poa amount or tax year from request"))
    }
  }

  private def transformFinancialDetailsResponse(
      amount:                   BigDecimal,
      financialUrl:             String,
      nino:                     String,
      financialDetailsResponse: Future[Option[DataModel]]
    ): Unit = {
    financialDetailsResponse.map {
      case Some(value) =>
        value.response match {
          case Some(response) =>
            performDataChanges(response, amount, financialUrl)
            Logger("application").info(s"Overwrote totalAmount data for $nino with new amount $amount")
          case None =>
            Future.failed(new Exception("Could not find response in financial details 1553 data for this nino"))
        }
      case None => Future.failed(new Exception("Could not find financial details 1553 data for this nino"))
    }
  }

  private def performDataChanges(
      response:     JsValue,
      amount:       BigDecimal,
      financialUrl: String
    ): Future[result.UpdateResult] = {
    //Create new 1553 data with totalAmount overwritten with new poa amount
    val newResponse = response.transform(transformDocDetails(amount)).getOrElse(response)
    //Overwrite existing 1553 data with the new poa amount
    dataRepository.replaceOne(url = financialUrl, updatedFile = getFinDetailsDataModel(newResponse, financialUrl))
  }

}
