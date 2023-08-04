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
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.net.URI
import javax.inject.{Inject, Singleton}

@Singleton
class AddPropertyController @Inject()(cc: MessagesControllerComponents,
                                      requestHandlerController: RequestHandlerController
                                     ) extends FrontendController(cc) with Logging {

  def mapAddPropertyJourneyStub(mtdid: String): Action[AnyContent] = Action.async {
    implicit request =>
      val testHeader = request.headers.get("Gov-Test-Scenario")

      if(testHeader.contains("afterIncomeSourceCreated")) {
        val newRequest = request.withTarget(request.target.withUri(URI.create(request.uri+"?afterIncomeSourceCreated=true")))
        requestHandlerController.getRequestHandler(newRequest.uri).apply(newRequest)
      } else {
        requestHandlerController.getRequestHandler(request.uri).apply(request)
      }
    }
}