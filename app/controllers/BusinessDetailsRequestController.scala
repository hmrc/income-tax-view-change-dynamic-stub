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

import models.BusinessDetailsModel
import org.apache.pekko.actor.ActorSystem
import play.api.libs.json.Json
import play.api.{Configuration, Logging}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MessagesRequest}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.{AddDelays, BusinessDataUtils}

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.DurationInt

@Singleton
class BusinessDetailsRequestController @Inject()(cc: MessagesControllerComponents,
                                                 requestHandlerController: RequestHandlerController,
                                                 dataRepository: DataRepository)
                                                 (implicit val ec: ExecutionContext,
                                                  val actorSystem: ActorSystem,
                                                  val configuration: Configuration)
    extends FrontendController(cc) with Logging with AddDelays {

  def overrideBusinessDetailsUrl(mtdid: String): String = {
    s"/etmp/RESTAdapter/itsa/taxpayer/business-details?mtdReference=$mtdid"
  }

  private def addSuffixToRequest(key: String, suffix: String)(implicit request: MessagesRequest[AnyContent]) = {
    val testHeader     = request.headers.get("Gov-Test-Scenario")
    val computedSuffix = if (testHeader.contains(key)) s"&$suffix" else ""
    val uri            = request.uri + computedSuffix
    val newRequest     = request.withTarget(request.target.withUri(URI.create(uri)))
    requestHandlerController.getRequestHandler(uri, Some(450.milliseconds)).apply(newRequest)
  }

  def transform(mtdReference: Option[String]): Action[AnyContent] =
    Action.async { implicit request =>
      addSuffixToRequest("afterIncomeSourceCreated", "afterIncomeSourceCreated=true")
      addSuffixToRequest("afterMigration", "afterMigration=true")
    }

  def overwriteBusinessData(mtdid: String): Action[AnyContent] =
    Action.async { implicit request =>
      request.body.asJson match {
        case None =>
          Future.successful(BadRequest("No JSON found - Expected JSON data"))

        case Some(json) =>
          json.validate[BusinessDetailsModel].fold(
            invalid = _ =>
              Future.successful(BadRequest("Invalid JSON data")),

            valid = userModel => {
              val url = overrideBusinessDetailsUrl(mtdid)

              val businessData  = BusinessDataUtils.createBusinessData(userModel.activeSoleTrader, userModel.ceasedBusiness)
              val propertyData = BusinessDataUtils.createPropertyData(userModel.activeUkProperty, userModel.activeForeignProperty)

              for {
                businessUpdate <- dataRepository.clearAndReplace(url, BusinessDataUtils.businessDataKey, businessData)
                propertyUpdate <- dataRepository.clearAndReplace(url, BusinessDataUtils.propertyDataKey, propertyData)
              } yield {
                (businessUpdate.wasAcknowledged(), propertyUpdate.wasAcknowledged()) match {
                  case (true, true) =>
                    logger.info("Successfully updated business details")
                    Ok("Success")

                  case (false, false) =>
                    logger.warn("Failed to update both business and property details")
                    InternalServerError("Failed to update business and property details")

                  case (false, true) =>
                    logger.warn("Failed to update business details")
                    InternalServerError("Failed to update business details")

                  case (true, false) =>
                    logger.warn("Failed to update property details")
                    InternalServerError("Failed to update property details")
                }
              }
            }
          ).recover {
            case ex =>
              logger.error("Unexpected error updating business data", ex)
              InternalServerError(s"Unexpected error occurred")
          }
      }
    }
}
