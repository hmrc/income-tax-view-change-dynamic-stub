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
import models.User

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future


@Singleton
class HomeController @Inject()(mcc: MessagesControllerComponents,
                               loginPage: LoginPage
                              ) extends FrontendController(mcc) with Logging {

  val dummyNinoList = List(
    "AA000000A Scottish tax calc",
    "AA111111A Chrystallised",
    "AA333333A Default",
    "AA888888A Default",
    "AY111111A First year - update submitted, not migrated",
    "AY222222A First year - Migrated with previous outstanding balance - User has both CESA and POA/BCD charges",
    "AY333333A First year - Migrated with previous outstanding balances and forecast calc",
    "AY444444A First year - Not migrated with missing first quarter update",
    "AY555555A First year - Migrated with BCD accruing interest",
    "AY666666A First year - Signed up with no updates",
    "AY777777A First year - Has unallocated credit from previous tax year",
    "BS000000A Second year - has not cleared the generated balancing charge and POAs",
    "BS111111A Second year - has cleared their generated balancing charge and POA1 but not the POA2",
    "BS222222A Second year - has only part paid their balancing charge",
    "BS333333A Second year - has cleared all charges",
    "BS444444A Second year - has an overdue charge amount for the current tax year and the charge is under investigation (dunning lock present)",
    "BS555555A Second year - has an overdue charge amount for the current tax year and has an Interest amount being investigated (Interest lock present)",
    "BS666666A Second year - has paid an overdue charge in full in the current tax year and a Late Payment interest charge is created",
    "BS777777A Second year - has partially paid an overdue charge for the current tax year and the accruing interest is still accumulating in the the amount unpaid",
    "BS888888A Second year - has a charge that is overdue for more than one tax year, therefore accruing interest will span more than one tax year",
    "CC111111A Coding out immediately rejected",
    "CC222222A Coding out accepted then rejected part way through year",
    "CC333333A Coding out partially collected",
    "CC444444A Coding out fully collected",
    "CC555555A Coding out requested and accepted",
    "AY888881A Refund claimed for partial amount and claim is in a pending state",
    "AY888882A Refund for full amount claimed and claim in a pending state",
    "AY888883A Multiple credit items/payments",
    "AY888884A ???",
    "AY888885A User with Single Payment (in credit?)",
    "AY888886A One Cut over Credit",
    "MA999991A MFA -",
    "MA999992A MFA - User with Single MFA Credit",
    "MA999993A MFA -",
    "LA000064A QA user1",
    "LA000069A QA user2",
    "KY661788D QA user3",
    "PW892533A QA user4",
    "PA000000A Payment Allocations Test",
    "PW886433A MFA Debit/credit",
    "PW871233A QA test 5",
    "PW902133A QA MFA Credit Test",
    "PW950133A QA Coding Out Test",
    "PW898033A QA MFA Credit Test 2",
    "PW953333A QA Coding Test 2",
    "PW904033A MFA Credit Interest"
  )

  val getLogin: Action[AnyContent] = Action.async { implicit request =>
    Future.successful(Ok(loginPage(dummyNinoList, User.form.fill(User("", false)))))    // TODO: We will need to replace "dummyNinoList" with a list of ninos pulled from the text file
  }

  val postLogin: Action[AnyContent] = Action.async { implicit request =>
    User.form.bindFromRequest().fold(
      formWithErrors =>
        Future.successful(BadRequest(s"Invalid form submission: $formWithErrors")),
      user =>
        Future.successful(Ok(s"Valid form submission: $user"))  // TODO: Pull ALL required details for the user from txt file here i.e. redirecturl, UTR etc and make the POST request to AUTH here
    )
  }
}