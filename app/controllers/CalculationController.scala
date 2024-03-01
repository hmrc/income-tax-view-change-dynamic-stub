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


import models.HttpMethod.GET
import models.{CalcSuccessReponse, CrystallisationStatus, DataModel, TaxYear}
import org.mongodb.scala.model.Filters.equal
import play.api.libs.json.{JsValue, Json, OWrites}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logger, Logging}
import repositories.{DataRepository, DefaultValues}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.CalculationUtils.{createCalResponseModel, getFallbackUrlLegacy, getFallbackUrlTYS, getTaxYearRangeEndYear}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class CalculationController @Inject()(cc: MessagesControllerComponents,
                                      dataRepository: DataRepository,
                                      requestHandlerController: RequestHandlerController,
                                      configuration: Configuration,
                                      defaultValues: DefaultValues)
                                     (implicit val ec: ExecutionContext)
  extends FrontendController(cc) with Logging {

  implicit val calcSuccessResponseWrites: OWrites[CalcSuccessReponse] = Json.writes[CalcSuccessReponse]

  def getCalcLegacy(nino: String, calcId: String): Action[AnyContent] = Action.async { _ =>
    val id = s"/income-tax/view/calculations/liability/$nino/$calcId"
    dataRepository
      .find(equal("_id", id), equal("method", GET))
      .flatMap {
        case stubData@Some(dataModel: DataModel) =>
          dataModel.response match {
            case Some(_: JsValue) => Future(Status(stubData.head.status)(stubData.head.response.get))
            case None => Logger("application").info(s"[CalculationController][getCalcLegacy] " +
              s"Could not find endpoint in Dynamic Stub matching the URI: $id . Calling fallback default endpoint.")
              Future.successful(Status(NO_CONTENT))
          }
        case None =>
          Logger("application").info(s"[CalculationController][getCalcLegacy] " +
            s"Could not find endpoint in Dynamic Stub matching the URI: $id . Calling fallback default endpoint.")
          val fallbackUrl: String = getFallbackUrlLegacy(calcId = calcId)
          defaultValues.getDefaultRequestHandler(url = fallbackUrl)
      }.recoverWith {
      case _ => Future.successful(BadRequest(s"Search operation failed: $id"))
    }
  }

  def generateCalculationListTYS(nino: String, taxYearRange: String): Action[AnyContent] = {

    val stubbed1896NinoPrefixes: Seq[String] = configuration.getOptional[Seq[String]]("stubbed1896NinoPrefixes")
      .getOrElse(Seq.empty)

    if (stubbed1896NinoPrefixes.exists(prefix => nino.startsWith(prefix))) {
      // Retrieve stubbed response from ATs
      requestHandlerController.getRequestHandler(s"/income-tax/view/calculations/liability/$taxYearRange/$nino")
    } else {
      Action.async { _ =>
        Logger("application").info(s"Generating calculation list for nino: $nino")
        Future {
          createCalResponseModel(nino, Some(getTaxYearRangeEndYear(taxYearRange)), crystallised = true) match {
            case Right(responseModel) =>
              val jsonReponse = Json.toJson(responseModel).toString()
              Ok(Json.parse(jsonReponse))
            case Left(error) =>
              BadRequest(s"Failed with error: $error")
          }
        }
      }
    }
  }

  def getCalculationDetailsTYS(nino: String, calculationId: String, taxYearRange: String): Action[AnyContent] = Action.async { _ =>
    Logger("application").info(s"Generating calculation details for nino: $nino calculationId: $calculationId")
    val id = s"/income-tax/view/calculations/liability/$taxYearRange/$nino/${calculationId.toLowerCase()}"
    dataRepository
      .find(equal("_id", id), equal("method", GET))
      .flatMap {
        case stubData@Some(dataModel: DataModel) =>
          dataModel.response match {
            case Some(_: JsValue) => Future(Status(stubData.head.status)(stubData.head.response.get))
            case None => Logger("application").info(s"[CalculationController][getCalculationDetailsTYS] " +
              s"Could not find endpoint in Dynamic Stub matching the URI: $id . Calling fallback default endpoint.")
              Future.successful(Status(NO_CONTENT))
          }
        case None =>
          Logger("application").info(s"[CalculationController][getCalculationDetailsTYS] " +
            s"Could not find endpoint in Dynamic Stub matching the URI: $id . Calling fallback default endpoint.")
          val fallbackUrl: String = getFallbackUrlTYS(taxYearRange = taxYearRange)
          defaultValues.getDefaultRequestHandler(url = fallbackUrl)
      }.recoverWith {
      case _ => Future.successful(BadRequest(s"Search operation failed: $id"))
    }
  }

  def createOverwriteCalculationListUrl(nino: String, taxYear: TaxYear): String = {
    if (taxYear.endYear >= 2024) {
      Logger("application").info(s"[CalculationController][createOverwriteCalculationListUrl] Overwriting calculation details TYS")
      s"/income-tax/view/calculations/liability/${taxYear.formattedTaxYearRange}/$nino"
    } else {
      Logger("application").info(s"[CalculationController][createOverwriteCalculationListUrl] Overwriting calculation details legacy")
      s"/income-tax/list-of-calculation-results/$nino?taxYear=${taxYear.endYearString}"
    }
  }

  def overwriteCalculationList(nino: String, taxYearRange: String, crystallisationStatus: String): Action[AnyContent] = Action.async { _ =>
    Logger("application").info(s"Overwriting calculation list data for < nino: $nino > < taxYearRange: $taxYearRange > < crystallisationStatus: $crystallisationStatus >")

    TaxYear.createTaxYearGivenTaxYearRange(taxYearRange) match {
      case Some(taxYear) =>

        val url = createOverwriteCalculationListUrl(nino, taxYear)
        val crystallisationStatusObj = CrystallisationStatus(crystallisationStatus, nino, taxYear, url)

        dataRepository.replaceOne(url = crystallisationStatusObj.url, updatedFile = crystallisationStatusObj.makeOverwriteDataModel)
          .map { result =>
            if (result.wasAcknowledged) {
              Logger("application").info(s"[CalculationController][overwriteCalculationList] Overwrite success! For < url: $url >")
              Ok("Success")
            } else {
              Logger("application").error(s"[CalculationController][overwriteCalculationList] Write was not acknowledged! For < url: $url >")
              InternalServerError("Write was not acknowledged")
            }
          }.recoverWith {
          case ex =>
            Logger("application").error(s"[CalculationController][overwriteCalculationList] Update operation failed. < Exception: $ex >")
            Future.failed(ex)
        }
      case None =>
        Logger("application").error(s"[CalculationController][overwriteCalculationList] taxYearRange could not be converted to TaxYear")
        Future.failed(new Exception("taxYearRange could not be converted to TaxYear"))
    }

  }
}