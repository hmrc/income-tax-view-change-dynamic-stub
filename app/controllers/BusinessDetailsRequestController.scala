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

import org.apache.pekko.actor.ActorSystem
import play.api.{Configuration, Logging}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MessagesRequest}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.AddDelays

import java.net.URI
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.DurationInt

@Singleton
class BusinessDetailsRequestController @Inject()(cc: MessagesControllerComponents,
                                                 requestHandlerController: RequestHandlerController)
                                                 (implicit val ec: ExecutionContext,
                                                  val actorSystem: ActorSystem,
                                                  val configuration: Configuration)
    extends FrontendController(cc) with Logging with AddDelays {

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
    }
}
