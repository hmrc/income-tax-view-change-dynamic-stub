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
import views.html.LoginPage

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future


@Singleton
class HomeController @Inject()(mcc: MessagesControllerComponents,
                               loginPage: LoginPage
                              ) extends FrontendController(mcc) with Logging {

  val ninoList = List(
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

  val getLogin: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(loginPage(ninoList)))
  }

}
