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


import models.HttpMethod.GET
import org.mongodb.scala.model.Filters.equal
import play.api.Logging
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import repositories.DataRepository
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController
import utils.GetCalculationListUtils.{getCalculationListSuccessResponse, ninoMatchCharacters}

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

@Singleton
class CalculationController @Inject()(
                                       cc: ControllerComponents,
                                       dataRepository: DataRepository,
                                     ) extends BackendController(cc) with Logging {

  def generateCalculationList(nino: String): Action[AnyContent] = Action.async { _ =>
    logger.info(s"Generating calculation list for nino: $nino,")
    Future(Ok(Json.parse(getCalculationListSuccessResponse(ninoMatchCharacters(nino), Some(2024), true))))
  }

  def getCalculationDetails(nino: String, calculationId: String): Action[AnyContent] = Action.async { _ =>
    logger.info(s"Generating calculation details for nino: $nino calculationId: $calculationId")
    val id = s"/income-tax/view/calculations/liability/23-24/$nino/${calculationId.toLowerCase()}"
    dataRepository.find(equal("_id", id), equal("method", GET)).map {
      stubData =>
        if (stubData.nonEmpty) {
          if (stubData.head.response.isEmpty) {
            Status(stubData.head.status)
          } else {
            Status(stubData.head.status)(stubData.head.response.get)
          }
        } else {
          NotFound(s"Could not find endpoint in Dynamic Stub matching the URI: $id")
        }
    }


  }
}
