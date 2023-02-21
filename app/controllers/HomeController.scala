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

import config.MicroserviceAuthConnector
import models.User
import play.api.Logging
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.TooManyRequestException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.SessionBuilder
import views.html.LoginPage

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class HomeController @Inject()(mcc: MessagesControllerComponents,
                               loginPage: LoginPage,
                               microserviceAuthConnector: MicroserviceAuthConnector
                              ) extends FrontendController(mcc) with Logging {

  val dummyNinoList: List[String] = List("CC333333A") //TODO: add more ninos to the list and details for those

  val getLogin: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(loginPage(dummyNinoList))) // TODO: We will need to replace "dummyNinoList" with a list of ninos pulled from the text file
  }

  val postLogin: Action[AnyContent] = Action.async { implicit request =>
    User.form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(s"Invalid form submission: $formWithErrors")),
      user =>
        microserviceAuthConnector.login(nino = user.nino) map {
          case (authExchange, _) =>
            println("@@@" + SessionBuilder.buildGGSession(authExchange))
            Redirect("http://localhost:9081/report-quarterly/income-and-expenses/view?origin=BTA")
              .withSession(SessionBuilder.buildGGSession(authExchange))
        }
    ).recoverWith {
      case exception: TooManyRequestException =>
        Future.successful(TooManyRequests(exception.getMessage))
      case exception: Exception =>
        Future.successful(BadGateway(exception.getMessage))
    }
  }

}