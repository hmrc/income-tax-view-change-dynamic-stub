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

import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class UpdateIncomeSourceController @Inject() (
    cc:                       MessagesControllerComponents,
    requestHandlerController: RequestHandlerController)
    extends FrontendController(cc) {

  def mapUpdateIncomeSourceStub(): Action[AnyContent] =
    Action.async { implicit request =>
      val bodyOption: Option[JsValue] = request.body.asJson
      val InvalidJsonError = BadRequest("Invalid JSON in the UpdateIncomeSourceRequest body")
      val IncomeSourceIdNotFoundError =
        BadRequest("Income Source ID not found in the UpdateIncomeSourceRequest body")

      bodyOption match {
        case Some(body) =>
          val incomeSourceIdOption = extractIncomeSourceId(body)

          incomeSourceIdOption match {
            case Some(incomeSourceId) =>
              val newRequest =
                if (incomeSourceId.toLowerCase.contains("error")) {
                  // Retrieve stubbed error response from ATs
                  request.withTarget(request.target.withUri(URI.create(request.uri + s"?id=$incomeSourceId")))
                } else {
                  // Retrieve stubbed success response from ATs
                  request
                }
              requestHandlerController.putRequestHandler(newRequest.uri).apply(newRequest)
            case None => Future.successful(IncomeSourceIdNotFoundError)
          }
        case None => Future.successful(InvalidJsonError)
      }
    }

  def extractIncomeSourceId(json: JsValue): Option[String] = {
    (json \ "incomeSourceID").asOpt[String]
  }
}
