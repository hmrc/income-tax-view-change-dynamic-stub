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

import models.{ItsaStatus, TaxYear}
import play.api.{Logger, Logging}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItsaStatusController @Inject()(cc: MessagesControllerComponents,
                                     dataRepository: DataRepository)
                                    (implicit val ec: ExecutionContext)
  extends FrontendController(cc) with Logging {

  private def createOverwriteItsaStatusUrl(nino: String, taxYear: TaxYear): String = {
    s"/income-tax/$nino/person-itd/itsa-status/${taxYear.formattedTaxYearRange}?futureYears=false&history=false"
  }

  def overwriteItsaStatus(nino: String, taxYearRange: String, itsaStatus: String): Action[AnyContent] = Action.async { _ =>
    Logger("application").info(s"Overwriting itsa status data for < nino: $nino > < taxYearRange: $taxYearRange > < itsaStatus: $itsaStatus >")

    TaxYear.createTaxYearGivenTaxYearRange(taxYearRange) match {
      case Some(taxYear: TaxYear) =>

        val url = createOverwriteItsaStatusUrl(nino = nino, taxYear = taxYear)
        val itsaStatusObj = ItsaStatus(itsaStatus, url, taxYear)

        dataRepository.replaceOne(url = url, updatedFile = itsaStatusObj.makeOverwriteDataModel).map { result =>
          if (result.wasAcknowledged) {
            Logger("application").info(s"[ItsaStatusController][overwriteItsaStatus] Overwrite success! For < url: $url >")
            Ok(s"Overwrite success! For < url: $url >")
          } else {
            Logger("application").info(s"[ItsaStatusController][overwriteItsaStatus] Write was not acknowledged! For < url: $url >")
            InternalServerError("Write was not acknowledged")
          }
        }.recoverWith {
          case ex =>
            Logger("application").error(s"[ItsaStatusController][overwriteItsaStatus] Update operation failed. < Exception: $ex >")
            Future.failed(ex)
        }
      case None =>
        Logger("application").error(s"[ItsaStatusController][overwriteItsaStatus] taxYearRange could not be converted to TaxYear")
        Future.failed(new Exception("taxYearRange could not be converted to TaxYear"))
    }
  }

}
