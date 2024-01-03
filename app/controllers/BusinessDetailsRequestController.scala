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

import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, MessagesRequest}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.net.URI
import javax.inject.{Inject, Singleton}

@Singleton
class BusinessDetailsRequestController @Inject()(cc: MessagesControllerComponents,
                                                 requestHandlerController: RequestHandlerController
                                                ) extends FrontendController(cc) with Logging {

  private def addSuffixToRequest(key: String, suffix: String)(implicit request: MessagesRequest[AnyContent]) = {
    val testHeader = request.headers.get("Gov-Test-Scenario")
    val computedSuffix = if (testHeader.contains(key)) s"?$suffix" else ""
    val uri = request.uri + computedSuffix
    val newRequest = request.withTarget(request.target.withUri(URI.create(uri)))
    requestHandlerController.getRequestHandler(uri).apply(newRequest)
  }

  def transform(mtdid: String): Action[AnyContent] = Action.async {
    implicit request =>
      addSuffixToRequest("afterIncomeSourceCreated", "afterIncomeSourceCreated=true")
  }

  def transformNinoCall(nino: String): Action[AnyContent] = Action.async {
    implicit request =>
      val newRequest = request.withTarget(request.target.withUri(URI.create(request.uri)))
      requestHandlerController.getRequestHandler(request.uri).apply(newRequest)
  }
}
