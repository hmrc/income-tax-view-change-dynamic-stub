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

import models.ItsaStatus
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import play.api.{Configuration, Logging}
import repositories.{DataRepository, DefaultValues}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItsaStatusController @Inject()(cc: MessagesControllerComponents,
                                     dataRepository: DataRepository)
                                    (implicit val ec: ExecutionContext)
  extends FrontendController(cc) with Logging {

  private def createOverwriteItsaStatusUrl(nino: String, taxYearRange: String): String = {
    s"/income-tax/$nino/person-itd/itsa-status/$taxYearRange?futureYears=false&history=false"
  }

  def overwriteItsaStatus(nino: String, taxYearRange: String, itsaStatus: String): Action[AnyContent] = Action.async { _ =>

    val url = createOverwriteItsaStatusUrl(nino = nino, taxYearRange = taxYearRange)

    val itsaStatusObj = ItsaStatus(itsaStatus, url, taxYearRange)

    println("AAAAAAAAAA\n" + itsaStatus + "\n" + url + "\n" + itsaStatusObj)

    dataRepository.replaceOne(url = url, updatedFile = itsaStatusObj.makeOverwriteDataModel).map { result =>
      if (result.wasAcknowledged) {
        Ok("Success")
      } else {
        InternalServerError("Write was not acknowledged")
      }
    }.recoverWith {
      case ex => Future.successful(BadRequest(s"Update operation failed $ex"))
    }
  }

}
