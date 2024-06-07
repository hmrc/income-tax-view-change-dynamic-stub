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
import play.api.libs.json.{JsObject, JsValue}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger, Logging}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import play.api.libs.json._
import play.api.libs.json.Reads._

import java.net.URI
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SubmitPoaController @Inject()(cc: MessagesControllerComponents,
                                    requestHandlerController: RequestHandlerController,
                                    configuration: Configuration,
                                    dataRepository: DataRepository
                                   )(implicit val ec: ExecutionContext)
  extends FrontendController(cc) with Logging {

  private val error1773Ninos: Seq[String] = configuration.getOptional[Seq[String]]("api1773ErrorResponseNinos")
    .getOrElse(Seq.empty)

  def interceptPoaSubmit(): Action[AnyContent] = Action.async { implicit request =>

    val bodyOption: Option[JsValue] = request.body.asJson
    val InvalidJsonError = BadRequest("Invalid JSON in the UpdateIncomeSourceRequest body")
    val NoNinoError = BadRequest("Nino not found in the ClaimToAdjustPoaRequest body")

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
      case (Some(amount), Some(taxYearString)) => {
        TaxYear.createTaxYearGivenTaxYearRange(taxYearString) match {
          case Some(taxYear) => {
            val url = getFinancialDetailsUrl(nino, taxYear)
            val financialDetailsResponse = dataRepository.find(equal("_id", url))
            financialDetailsResponse.map {
              case Some(value) => value.response match {
                case Some(response) =>
                  //Create new 1553 data with totalAmount overwritten with new poa amount
                  val newResponse = response.transform(transformArray(amount.toInt)).getOrElse(response)
                  //Overwrite existing 1553 data
                  dataRepository.replaceOne(url = url, updatedFile = getDataModel(newResponse, url))
                case None =>
                  Future.failed(new Exception("Could not find response in financial details 1553 data for this nino"))
              }
              case None => Future.failed(new Exception("Could not find financial details 1553 data for this nino"))
            }
          }
          case None => Future.failed(new Exception("Failed to create tax year from request"))
        }
      }
      case _ => Future.failed(new Exception("Could not extract poa amount or tax year from request"))
    }
  }

  private def extractNino(json: JsValue): Option[String] = {
    (json \ "nino").asOpt[String]
  }

  private def extractPoAAmount(json: JsValue): Option[BigDecimal] = {
    (json \ "amount").asOpt[BigDecimal]
  }

  private def extractTaxYear(json: JsValue): Option[String] = {
    (json \ "taxYear").asOpt[String]
  }

  private def getFinancialDetailsUrl(nino: String, taxYear: TaxYear): String = {
    s"/enterprise/02.00.00/financial-data/NINO/$nino/ITSA?dateFrom=${taxYear.startYear}-04-06&dateTo=${taxYear.endYear}-04-05&onlyOpenItems=false&includeLocks=true&calculateAccruedInterest=true&removePOA=false&customerPaymentInformation=true&includeStatistical=false"
  }

  private def transformArray(amount: Int): Reads[JsObject] = {
    (__ \ "documentDetails").json.update(
      of[JsArray].map {
        case JsArray(arr) =>
          JsArray(arr.map(item => item.transform(transformAmount(amount)).getOrElse(item)))
      }
    )
  }

  private def transformAmount(amount: Int): Reads[JsObject] = {
    (__ \ "totalAmount").json.update(
      of[JsNumber].map {
        case JsNumber(_) =>
          JsNumber(amount)
      }
    )
  }

  def getDataModel(request: JsValue, url: String): DataModel = DataModel(
    _id = url,
    schemaId = "getFinancialDetailsSuccess",
    method = "GET",
    status = 200,
    response = Some(request)
  )
}
