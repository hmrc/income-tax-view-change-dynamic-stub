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

import play.api.Configuration
import play.api.libs.json.JsValue
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.net.URI
import javax.inject.Inject
import scala.concurrent.Future

class SubmitPoaController @Inject()(cc: MessagesControllerComponents,
                                    requestHandlerController: RequestHandlerController,
                                    configuration: Configuration
                                   ) extends FrontendController(cc) {

  val error1773Ninos: Seq[String] = configuration.getOptional[Seq[String]]("api1773ErrorResponseNinos")
    .getOrElse(Seq.empty)

  def interceptSubmitForErrorUser(): Action[AnyContent] = Action.async { implicit request =>

    val bodyOption: Option[JsValue] = request.body.asJson
    val InvalidJsonError = BadRequest("Invalid JSON in the UpdateIncomeSourceRequest body")
    val NoNinoError = BadRequest("Nino not found in the ClaimToAdjustPoaRequest body")

    bodyOption match {
      case Some(body) =>
        val ninoOpt = extractNino(body)
        ninoOpt match {
          case Some(nino) =>
            val newRequest =
              if (error1773Ninos.contains(nino)) {
                // Retrieve stubbed error response from ATs
                request.withTarget(request.target.withUri(URI.create(request.uri + s"?nino=$nino")))
              } else {
                // Retrieve stubbed success response from ATs
                request
              }
            requestHandlerController.postRequestHandler(newRequest.uri).apply(newRequest)
          case None => Future.successful(NoNinoError)
        }
      case None => Future.successful(InvalidJsonError)
    }
  }

  def extractNino(json: JsValue): Option[String] = {
    (json \ "nino").asOpt[String]
  }
}
