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

import play.api.Logger
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController
import utils.EnrolmentStoreProxyResponse

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class EnrolmentStoreProxyController @Inject() (cc: MessagesControllerComponents) extends FrontendController(cc) {

  def getUTRList(groupId: String): Action[AnyContent] =
    Action.async {
      Logger("application").info(s"${Console.YELLOW} agent groupid found: $groupId" + Console.WHITE)
      if (groupId.isEmpty) {
        Future.successful(InternalServerError("Invalid groupId"))
      } else {
        val responseJson = EnrolmentStoreProxyResponse.generateResponse
        Logger("application").info(s"${Console.YELLOW} responseJson: $responseJson" + Console.WHITE)
        Future.successful(Ok(responseJson))
      }
    }
}
