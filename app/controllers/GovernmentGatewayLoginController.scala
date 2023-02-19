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
import models.Nino
import play.api.Logging
import play.api.data.Forms._
import play.api.data.{Form, Mapping}
import play.api.mvc.{Action, AnyContent, ControllerComponents, MessagesControllerComponents}
import uk.gov.hmrc.http.TooManyRequestException
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import utils.SessionBuilder

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}


object LoginForm {

  val dummyNinoList: List[String] = List(
    "AA000000A",
    "AA888888A",
    "BB222222A",
    "AA111111A",
    "AY111111A",
    "AY222222A",
    "AY333333A",
    "AY444444A",
    "AY555555A",
    "AY666666A",
    "AY777777A",
    "BS000000A",
    "BS111111A",
    "BS222222A",
    "BS333333A",
    "BS444444A",
    "BS555555A",
    "BS666666A",
    "BS777777A",
    "BS888888A",
    "CC111111A",
    "CC222222A",
    "CC333333A",
    "CC444444A",
    "CC555555A",
    "EC000000A",
    "EC111111A",
    "AY888881A",
    "AY888882A",
    "AY888883A",
    "AY888884A"
  )

  val ninoNonEmptyMapping: Mapping[Nino] = text.verifying("You must supply a valid Nino", nino => models.Nino.isValid(nino)).transform[Nino](Nino(_), _.value)
  val get: Form[Login] = Form(mapping("nino" -> ninoNonEmptyMapping, "isAgent" -> boolean)(Login.apply)(Login.unapply))

  case class Login(nino: Nino, isAgent: Boolean)
}
import views.html
@Singleton
class GovernmentGatewayLoginController @Inject()(cc: ControllerComponents,
                                                 governmentGatewayApiConnector: MicroserviceAuthConnector,
                                                 mcc: MessagesControllerComponents,
                                                 loginPage: LoginPage
                                                )(implicit appConfig: ServicesConfig, ec: ExecutionContext)
  extends BackendController(cc) with Logging {

  def governmentGatewayLogin(): Action[AnyContent] = Action.async { implicit request =>
    LoginForm.get.bindFromRequest().fold(
      errors =>
        Future.successful(BadRequest("bad request")),
      validForm => {
        governmentGatewayApiConnector.login(nino = validForm.nino) map {
          case (authExchange, _) =>
            println("@@@" + SessionBuilder.buildGGSession(authExchange))
            Redirect("http://localhost:9081/report-quarterly/income-and-expenses/view?origin=BTA")
              .withSession(SessionBuilder.buildGGSession(authExchange))
        }
      }
    ).recoverWith {
      case exception: TooManyRequestException =>
        Future.successful(TooManyRequests(""))
      case exception: Exception =>
        Future.successful(BadGateway(""))
    }

  }

  def getLogin: Action[AnyContent] = Action.async { _ =>
    Future.successful(Ok(loginPage(dummyNinoList, User.form.fill(User("", false)))))
  }

  //private def populate(credId: String): String = if (isBlank(credId)) randomNumeric(16) else credId

}

