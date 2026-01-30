/*
 * Copyright 2026 HM Revenue & Customs
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

import models.{CustomUserModel, DataModel}
import org.apache.pekko.actor.ActorSystem
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logging}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.AddDelays
import utils.CustomUserUtils.{convertCustomUserFields, createBusinessDetailsArray, createCalculationData, createPropertyData}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CustomUserController @Inject()(cc: MessagesControllerComponents,
                                     dataRepository: DataRepository)
                                    (implicit val ec: ExecutionContext,
                                     val actorSystem: ActorSystem,
                                     val configuration: Configuration)
  extends FrontendController(cc) with Logging with AddDelays {

  def overrideBusinessDetailsUrl(mtdid: String): String = {
    s"/etmp/RESTAdapter/itsa/taxpayer/business-details?mtdReference=$mtdid"
  }

  def overrideCalculationUrl(nino: String): String = {
    s"/income-tax/25-26/view/$nino/calculations-summary"
  }

  def overrideCustomUserData(nino: String, mtdid: String): Action[AnyContent] = Action.async { implicit request =>
    println(Console.MAGENTA + s"Received request to override custom user data for NINO=$nino MTDID=$mtdid" + Console.RESET)
    println(Console.MAGENTA + s"Request body: ${request.body.asJson}" + Console.RESET)
    request.body.asJson.map { json =>
      json.validate[CustomUserModel].fold(
        invalid => {
          println(Console.RED + s"Failed to parse JSON for NINO=$nino MTDID=$mtdid: $invalid" + Console.RESET)
          Future.successful(BadRequest("Invalid JSON data"))
        },
        userModel => {
          println(Console.GREEN + s"Successfully parsed JSON for NINO=$nino MTDID=$mtdid: $userModel" + Console.RESET)
          val convertedModel = convertCustomUserFields(userModel)
          println(Console.GREEN + s"Converted model for NINO=$nino MTDID=$mtdid: $convertedModel" + Console.RESET)

          val activeSoleTraders = createBusinessDetailsArray(convertedModel.numberOfActiveBusinesses, isCeased = false)
          val ceasedSoleTraders = createBusinessDetailsArray(convertedModel.numberOfCeasedBusinesses, isCeased = true)

          val propertyData = createPropertyData(convertedModel.ukPropertyCheckbox, convertedModel.foreignPropertyCheckbox, userModel.numberOfCeasedUkProperties, userModel.numberOfCeasedForeignProperties)

          val calculationData = createCalculationData(convertedModel.calculationTypeLatest, convertedModel.calculationTypePrevious)

          dataRepository.updateOneById(overrideBusinessDetailsUrl(mtdid), convertedModel)
          dataRepository.clearAndReplace(overrideBusinessDetailsUrl(mtdid), "response.success.taxPayerDisplayResponse.businessData", activeSoleTraders ++ ceasedSoleTraders)
          dataRepository.clearAndReplace(overrideBusinessDetailsUrl(mtdid), "response.success.taxPayerDisplayResponse.propertyData", propertyData)
          dataRepository.clearAndReplace(overrideCalculationUrl(nino), "response.calculationsSummary", calculationData)

          println(Console.GREEN + s"Updated custom user data for ${overrideBusinessDetailsUrl(mtdid)}" + Console.RESET)
          Future.successful(Ok("Success"))
        }
      )
    }.getOrElse {
      println(Console.RED + s"Expected JSON data for NINO=$nino MTDID=$mtdid but none found" + Console.RESET)
      Future.successful(BadRequest("Expected JSON data"))
    }
  }
}
