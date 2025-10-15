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

import models.HttpMethod.GET
import models.{ItsaStatus, Nino, TaxYear}
import org.mongodb.scala.model.Filters.equal
import play.api.{Logger, Logging}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.{DataRepository, DefaultValues}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ItsaStatusController @Inject() (
    cc:             MessagesControllerComponents,
    dataRepository: DataRepository,
    defaultValues:  DefaultValues
  )(
    implicit val ec: ExecutionContext)
    extends FrontendController(cc)
    with Logging {

  private def createOverwriteItsaStatusUrl(nino: String, taxYear: TaxYear): String = {
    s"/income-tax/$nino/person-itd/itsa-status/${taxYear.rangeShort}?futureYears=false&history=false"
  }

  private def createOverwriteHipItsaStatusUrl(nino: String, taxYear: TaxYear): String = {
    s"/itsd/person-itd/itsa-status/$nino?taxYear=${taxYear.rangeShort}&futureYears=false&history=false"
  }

  def getHIPITSAStatus(
      nino:         String,
      taxYearRange: String,
      futureYears:  Boolean,
      history:      Boolean
    ): Action[AnyContent] = {
    Action.async { implicit request =>
      dataRepository.find(equal("_id", request.uri), equal("method", GET)).map { stubData =>
        stubData.headOption match {
          case Some(datamodel) if datamodel.response.nonEmpty =>
            Thread.sleep(125)
            Status(datamodel.status)(stubData.head.response.get)
          case Some(dataModel) =>
            Thread.sleep(125)
            Status(dataModel.status)
          case _               =>
            Thread.sleep(125)
            Status(OK)(defaultValues.getHipItsaStatusDefaultJson(taxYearRange))
        }
      }
    }
  }

  def overwriteItsaStatus(nino: String, taxYearRange: String, itsaStatus: String): Action[AnyContent] =
    Action.async { _ =>
      Logger("application").info(
        s"Overwriting itsa status data for < nino: $nino > < taxYearRange: $taxYearRange > < itsaStatus: $itsaStatus >"
      )

      TaxYear.createTaxYearGivenTaxYearRange(taxYearRange) match {
        case Some(taxYear: TaxYear) => overrideItsaStatusForTaxYear(taxYear, nino, itsaStatus)
        case None =>
          Logger("application").error(
            s"[ItsaStatusController][overwriteItsaStatus] taxYearRange could not be converted to TaxYear"
          )
          Future.failed(new Exception("taxYearRange could not be converted to TaxYear"))
      }
    }

  private def overrideItsaStatusForTaxYear(taxYear: TaxYear, nino: String, itsaStatus: String): Future[Result] = {
    val url              = createOverwriteItsaStatusUrl(nino = nino, taxYear = taxYear)
    val hipUrl           = createOverwriteHipItsaStatusUrl(nino = nino, taxYear = taxYear)
    val itsaStatusObj    = ItsaStatus(itsaStatus, url, taxYear, false)
    val itsaStatusHipObj = ItsaStatus(itsaStatus, hipUrl, taxYear, true)

    val res = for {
      ifStatusOverride <-
        dataRepository
          .replaceOne(url = url, updatedFile = itsaStatusObj.makeOverwriteDataModel)
      hipStatusOverride <-
        dataRepository
          .replaceOne(url = hipUrl, updatedFile = itsaStatusHipObj.makeOverwriteDataModel)
    } yield {
      (ifStatusOverride.wasAcknowledged(), hipStatusOverride.wasAcknowledged()) match {
        case (true, true) => Some(s"For < url: $url and $hipUrl>")
        case (true, true) => Some(s"For < url: $url>")
        case (true, true) => Some(s"For < url: $hipUrl>")
        case _            => None
      }
    }

    res
      .map {
        case Some(urlsUpdated) =>
          Logger("application").info(
            s"[ItsaStatusController][overwriteItsaStatus] Overwrite success! $urlsUpdated "
          )
          Ok(s"Overwrite success! $urlsUpdated")
        case _ =>
          Logger("application").info(
            s"[ItsaStatusController][overwriteItsaStatus] Write was not acknowledged! For < url: $url or $hipUrl>"
          )
          InternalServerError("Write was not acknowledged")
      }
      .recoverWith {
        case ex =>
          Logger("application").error(
            s"[ItsaStatusController][overwriteItsaStatus] Update operation failed. < Exception: $ex >"
          )
          Future.failed(ex)
      }
  }

}
