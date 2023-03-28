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
import play.api.Logging
import play.api.libs.json.{Json, OWrites}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.CalculationUtils.createCalResponseModel
import utils.GetCalculationListUtils.getCalculationListSuccessResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CalculationController @Inject()(cc: MessagesControllerComponents,
                                      dataRepository: DataRepository
                                     ) extends FrontendController(cc) with Logging {

  implicit val calcSuccessResponseWrites: OWrites[CalcSuccessReponse] = Json.writes[CalcSuccessReponse]

  def generateCalculationListFor2023_24(nino: String): Action[AnyContent] = Action.async { _ =>
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

  def getCalculationDetailsFor2023_24(nino: String, calculationId: String): Action[AnyContent] = Action.async { _ =>
    logger.info(s"Generating calculation details for nino: $nino calculationId: $calculationId")
    val id = s"/income-tax/view/calculations/liability/23-24/$nino/${calculationId.toLowerCase()}"
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

  def generateCalculationListLegacy(nino: String, taxYear: Option[Int]): Action[AnyContent] = Action.async { _ =>
    logger.info(s"Generating calculation list for nino: $nino, taxYear: $taxYear")

    val ninoMatchCharacters = (nino: String) => s"${nino.charAt(0)}${nino.charAt(7)}"

    ninoMatchCharacters(nino) match {
      case "L2" => Future(
        InternalServerError("""{"code": "SERVER_ERROR", "reason": "DES is currently experiencing problems that require live service intervention."}"""))
      case matchChars if matchChars.startsWith("L") => Future(
        NotFound("""{"code": "NOT_FOUND", "reason": "The remote endpoint has indicated that no data can be found."}"""))
      case "A1" => Future(Ok(Json.parse(getCalculationListSuccessResponse(ninoMatchCharacters(nino).toLowerCase, taxYear, crystallised = true))))
      //S0 is an exception as calculation ID only accepts characters from a-f
      case "S0" => Future(Ok(Json.parse(getCalculationListSuccessResponse("c9", taxYear, crystallised = true))))
      case _ => Future(Ok(Json.parse(getCalculationListSuccessResponse(ninoMatchCharacters(nino).toLowerCase, taxYear))))
    }
  }
}
