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


import models.{CalcSuccessReponse, DataModel}
import models.HttpMethod.GET
import org.mongodb.scala.model.Filters.equal
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import play.api.{Configuration, Logging}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.CalculationUtils.createCalResponseModel

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CalculationController @Inject()(cc: MessagesControllerComponents,
                                      dataRepository: DataRepository,
                                      requestHandlerController: RequestHandlerController,
                                      configuration: Configuration) extends FrontendController(cc) with Logging {

  implicit val calcSuccessResponseWrites: OWrites[CalcSuccessReponse] = Json.writes[CalcSuccessReponse]

  def generateCalculationListFor2023_24(nino: String): Action[AnyContent] = {

    val stubbed1896NinoPrefixes: Seq[String] = configuration.getOptional[Seq[String]]("stubbed1896NinoPrefixes")
      .getOrElse(Seq.empty)

    if (stubbed1896NinoPrefixes.exists(prefix => nino.startsWith(prefix))) {
      // Retrieve stubbed response from ATs
      requestHandlerController.getRequestHandler(s"/income-tax/view/calculations/liability/23-24/$nino")
    } else {
      Action.async { _ =>
        logger.info(s"Generating calculation list for nino: $nino")
        Future {
          createCalResponseModel(nino, Some(2024), crystallised = true) match {
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

  def getCalculationDetailsFor2023_24(nino: String, calculationId: String): Action[AnyContent] = Action.async { _ =>
    logger.info(s"Generating calculation details for nino: $nino calculationId: $calculationId")
    val id = s"/income-tax/view/calculations/liability/23-24/$nino/${calculationId.toLowerCase()}"
    dataRepository
      .find(equal("_id", id), equal("method", GET))
      .flatMap {
        case stubData@Some(_: DataModel) =>
          Future(Status(stubData.head.status)(stubData.head.response.get))
        case None =>
          logger.info(s"Could not find endpoint in Dynamic Stub matching the URI: $id . Calling fallback default endpoint.")
          getDefault
      }.recoverWith {
      case _ => Future {
        BadRequest(s"Search operation failed: $id")
      }
    }
  }

  def getDefault: Future[Result] = {
    val defaultId = "/income-tax/view/calculations/liability/23-24/SUCCESS1A/041f7e4d-87d9-4d4a-a296-3cfbdf2024a4"
    dataRepository
      .find(equal("_id", defaultId), equal("method", GET))
      .map {
        case stubData@Some(_: DataModel) =>
          Status(stubData.head.status)(stubData.head.response.get)
        case None =>
          NotFound(s"Failed to find the default API 1885 endpoint in Dynamic Stub matching the URI: $defaultId")
      }
  }
}
