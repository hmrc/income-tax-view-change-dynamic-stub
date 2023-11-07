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

package controllers


import models.CalcSuccessReponse
import models.HttpMethod.GET
import org.mongodb.scala.model.Filters.equal
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logging}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.CalculationUtils.{createCalResponseModel, getTaxYearRangeEndYear}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CalculationController @Inject()(cc: MessagesControllerComponents,
                                      dataRepository: DataRepository,
                                      requestHandlerController: RequestHandlerController,
                                      configuration: Configuration) extends FrontendController(cc) with Logging {

  implicit val calcSuccessResponseWrites: OWrites[CalcSuccessReponse] = Json.writes[CalcSuccessReponse]

  def getCalcLegacy(nino: String, calcId: String): Action[AnyContent] = {
    requestHandlerController.getRequestHandler(s"/income-tax/view/calculations/liability/$nino/$calcId")
  }

  def generateCalculationListTYS(nino: String, taxYearRange: String): Action[AnyContent] = {

    val stubbed1896NinoPrefixes: Seq[String] = configuration.getOptional[Seq[String]]("stubbed1896NinoPrefixes")
      .getOrElse(Seq.empty)

    if (stubbed1896NinoPrefixes.exists(prefix => nino.startsWith(prefix))) {
      // Retrieve stubbed response from ATs
      requestHandlerController.getRequestHandler(s"/income-tax/view/calculations/liability/$taxYearRange/$nino")
    } else {
      Action.async { _ =>
        logger.info(s"Generating calculation list for nino: $nino")
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
    logger.info(s"Generating calculation details for nino: $nino calculationId: $calculationId")
    val id = s"/income-tax/view/calculations/liability/$taxYearRange/$nino/${calculationId.toLowerCase()}"
    dataRepository
      .find(equal("_id", id), equal("method", GET))
      .map { stubData =>
        (stubData.nonEmpty, stubData.head.response.isEmpty) match {
          case (true, false) =>
            Status(stubData.head.status)(stubData.head.response.get)
          case _ =>
            NotFound(s"Could not find endpoint in Dynamic Stub matching the URI: $id")
        }
      }.recoverWith {
      case _ => Future {
        BadRequest(s"Search operation failed: $id")
      }
    }
  }
}
