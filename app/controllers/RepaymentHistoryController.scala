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

import play.api.{Configuration, Logger}
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendController

import java.time.{OffsetDateTime, ZoneOffset}
import javax.inject.{Inject, Singleton}

@Singleton
class RepaymentHistoryController @Inject()(
                                            requestHandlerController: RequestHandlerController,
                                            configuration: Configuration,
                                            mcc: MessagesControllerComponents
                                          ) extends FrontendController(mcc) {

  private val stubbedNinoPrefixes: Seq[String] =
    configuration.getOptional[Seq[String]]("stubbedRepaymentHistoryNinoPrefixes").getOrElse(Seq.empty)

  private def defaultHipResponse =
    Json.obj(
      "etmp_transaction_header" -> Json.obj(
        "status" -> "Approved",
        "processingDate" -> OffsetDateTime.now(ZoneOffset.UTC).toString
      ),
      "etmp_Response_Details" -> Json.obj(
        "repaymentsViewerDetails" -> Json.arr()
      )
    )

  def generateHipRepaymentHistoryByNino(nino: String): Action[AnyContent] = {
    if (stubbedNinoPrefixes.exists(nino.startsWith)) {
      requestHandlerController.getRequestHandler(s"/itsa/income-tax/v1/repayments/$nino")
    } else {
      Action { _ =>
        Logger("application").info(s"Generating HIP repayment history for nino: $nino")
        Ok(defaultHipResponse)
      }
    }
  }

  def generateHipRepaymentHistoryByRepaymentId(nino: String, repaymentId: String): Action[AnyContent] = {
    if (stubbedNinoPrefixes.exists(nino.startsWith)) {
      requestHandlerController.getRequestHandler(s"/itsa/income-tax/v1/repayments/$nino/repaymentId/$repaymentId")
    } else {
      Action { _ =>
        Logger("application").info(s"Generating HIP repayment history for nino: $nino, repaymentId: $repaymentId")
        Ok(defaultHipResponse)
      }
    }
  }
}

