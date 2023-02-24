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

package controllers.home

import config.MicroserviceAuthConnector
import models.User
import play.api.{Logger, Logging}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.http.TooManyRequestException
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.LoginUtil.{ reDirectURL}
import utils.{FileUtil, SessionBuilder}
import views.html.LoginPage

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


@Singleton
class HomeController @Inject()(mcc: MessagesControllerComponents,
                               loginPage: LoginPage,
                               microserviceAuthConnector: MicroserviceAuthConnector
                              ) extends FrontendController(mcc) with Logging {




  val getLogin: Action[AnyContent] = Action.async { implicit request =>
    FileUtil.getUsersFromFile("/data/users.txt") match {
      case Left(ex) =>
        Logger("application").error(s"[ITVC-Stub][getLogin] - Unable to read nino's: $ex")
        Future.successful(BadRequest(s"Unable to read nino's: $ex"))
      case Right(userRecords) =>
        Future.successful(Ok(loginPage(routes.HomeController.postLogin(), userRecords)))
    }
  }

  val postLogin: Action[AnyContent] = Action.async { implicit request =>
    User.form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(s"Invalid form submission: $formWithErrors")),
      user =>
        microserviceAuthConnector.login(nino = user.nino) map {
          case (authExchange, _) =>
            Redirect(reDirectURL)
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