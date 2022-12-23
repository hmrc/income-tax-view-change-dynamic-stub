/*
 * Copyright 2022 HM Revenue & Customs
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
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.GetCalculationDetailsUtils.getCalculationDetailsSuccessResponse
import utils.GetCalculationListUtils.{getCalculationListSuccessResponse, ninoMatchCharacters}

import javax.inject.Inject
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
class CalculationController @Inject()(
                                       cc: ControllerComponents,
                                     ) extends BackendController(cc) with Logging {

  def generateCalculationListFor23To24(nino: String): Action[AnyContent] = Action.async { _ =>
    logger.info(s"Generating calculation list for nino: $nino,")

    Future(Ok(Json.parse(getCalculationListSuccessResponse(ninoMatchCharacters(nino), Some(2024), true))))
  }

  def getCalculationDetails(nino: String, calculationId: String, taxYear: String): Action[AnyContent] = Action.async { _ =>
    taxYear match {
      case "23-24" =>
        logger.info(s"Generating calculation details for nino: $nino calculationId: $calculationId")
        val calcResponseStr = getCalculationDetailsSuccessResponse(ninoMatchCharacters(nino), Some(2024))
        Future(Ok(Json.parse(calcResponseStr)))
      case taxYear =>
        logger.error(s"Not yet supported taxYear: $taxYear")
        Future(NotFound(s"Not supported taxYear: $taxYear"))
    }
  }
}
