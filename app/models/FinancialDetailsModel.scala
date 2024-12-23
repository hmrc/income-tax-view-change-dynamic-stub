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

package models

import play.api.Logger
import play.api.libs.json.{Format, JsPath, Json, JsonValidationError, OWrites, Reads, Writes, __}
import play.api.libs.functional.syntax.toFunctionalBuilderOps

import java.time.LocalDate


case class FinancialDetailsModel(balanceDetails: BalanceDetails,
                                 private val documentDetails: List[DocumentDetail],
                                 financialDetails: List[FinancialDetail])

case class BalanceDetails(balanceDueWithin30Days: BigDecimal,
                          overDueAmount: BigDecimal,
                          totalBalance: BigDecimal,
                          availableCredit: Option[BigDecimal],
                          allocatedCredit: Option[BigDecimal],
                          firstPendingAmountRequested: Option[BigDecimal],
                          secondPendingAmountRequested: Option[BigDecimal],
                          unallocatedCredit: Option[BigDecimal]
                         )

case class DocumentDetail(taxYear: Int,
                          transactionId: String,
                          documentDescription: Option[String],
                          documentText: Option[String],
                          outstandingAmount: BigDecimal,
                          originalAmount: BigDecimal,
                          documentDate: LocalDate,
                          interestOutstandingAmount: Option[BigDecimal] = None,
                          interestRate: Option[BigDecimal] = None,
                          latePaymentInterestId: Option[String] = None,
                          interestFromDate: Option[LocalDate] = None,
                          interestEndDate: Option[LocalDate] = None,
                          latePaymentInterestAmount: Option[BigDecimal] = None,
                          lpiWithDunningLock: Option[BigDecimal] = None,
                          paymentLotItem: Option[String] = None,
                          paymentLot: Option[String] = None,
                          effectiveDateOfPayment: Option[LocalDate] = None,
                          amountCodedOut: Option[BigDecimal] = None,
                          documentDueDate: Option[LocalDate] = None,
                          poaRelevantAmount: Option[BigDecimal] = None
                         )

case class FinancialDetail(taxYear: String,
                           mainType: Option[String] = None,
                           mainTransaction: Option[String] = None,
                           transactionId: Option[String] = None,
                           transactionDate: Option[LocalDate] = None,
                           chargeReference: Option[String] = None,
                           `type`: Option[String] = None,
                           totalAmount: Option[BigDecimal] = None,
                           originalAmount: Option[BigDecimal] = None,
                           outstandingAmount: Option[BigDecimal] = None,
                           clearedAmount: Option[BigDecimal] = None,
                           chargeType: Option[String] = None,
                           accruedInterest: Option[BigDecimal] = None,
                           items: Option[Seq[SubItem]]
                          )

case class SubItem(dueDate: Option[LocalDate] = None,
                   subItemId: Option[String] = None,
                   amount: Option[BigDecimal] = None,
                   dunningLock: Option[String] = None,
                   interestLock: Option[String] = None,
                   clearingDate: Option[LocalDate] = None,
                   clearingReason: Option[String] = None,
                   clearingSAPDocument: Option[String] = None,
                   outgoingPaymentMethod: Option[String] = None,
                   paymentReference: Option[String] = None,
                   paymentAmount: Option[BigDecimal] = None,
                   paymentMethod: Option[String] = None,
                   paymentLot: Option[String] = None,
                   paymentLotItem: Option[String] = None,
                   paymentId: Option[String] = None,
                   transactionId: Option[String] = None)

object SubItem {

  implicit val writes: OWrites[SubItem] = Json.writes[SubItem]

  implicit val reads: Reads[SubItem] = for {
    subItemId <- (JsPath \ "subItemId").readNullable[String](Reads.of[String].filter(subItemJsonError)(isIntString))
    amount <- (JsPath \ "amount").readNullable[BigDecimal]
    dunningLock <- (JsPath \ "dunningLock").readNullable[String]
    interestLock <- (JsPath \ "interestLock").readNullable[String]
    clearingDate <- (JsPath \ "clearingDate").readNullable[String]
    clearingReason <- (JsPath \ "clearingReason").readNullable[String]
    clearingSAPDocument <- (JsPath \ "clearingSAPDocument").readNullable[String]
    outgoingPaymentMethod <- (JsPath \ "outgoingPaymentMethod").readNullable[String]
    paymentReference <- (JsPath \ "paymentReference").readNullable[String]
    paymentAmount <- (JsPath \ "paymentAmount").readNullable[BigDecimal]
    dueDate <- (JsPath \ "dueDate").readNullable[String]
    paymentMethod <- (JsPath \ "paymentMethod").readNullable[String]
    paymentLot <- (JsPath \ "paymentLot").readNullable[String]
    paymentLotItem <- (JsPath \ "paymentLotItem").readNullable[String]
  } yield {
    val id: Option[String] = for {
      pl <- paymentLot
      pli <- paymentLotItem
    } yield s"$pl-$pli"
    SubItem(
      dueDate.map(date => LocalDate.parse(date)),
      subItemId,
      amount,
      dunningLock,
      interestLock,
      clearingDate.map(date => LocalDate.parse(date)),
      clearingReason,
      clearingSAPDocument,
      outgoingPaymentMethod,
      paymentReference,
      paymentAmount,
      paymentMethod,
      paymentLot,
      paymentLotItem,
      id
    )
  }

  private def isIntString(s: String): Boolean = {
    try {
      s.toInt
      true
    } catch {
      case _: Exception =>
        Logger("application").warn(s"The returned 'subItem' field <$s> could not be parsed as an integer")
        false
    }
  }

  private def subItemJsonError: JsonValidationError = JsonValidationError(
    message = "The field 'subItem' should be parsable as an integer"
  )
}

object DocumentDetail {
  implicit val writes: Writes[DocumentDetail] = Json.writes[DocumentDetail]
  implicit val reads: Reads[DocumentDetail] = (
    (__ \ "taxYear").read[Int] and
      (__ \ "transactionId").read[String] and
      (__ \ "documentDescription").readNullable[String] and
      (__ \ "documentText").readNullable[String] and
      (__ \ "outstandingAmount").read[BigDecimal] and
      (__ \ "originalAmount").read[BigDecimal] and
      (__ \ "documentDate").read[LocalDate] and
      (__ \ "interestOutstandingAmount").readNullable[BigDecimal] and
      (__ \ "interestRate").readNullable[BigDecimal] and
      (__ \ "latePaymentInterestId").readNullable[String] and
      (__ \ "interestFromDate").readNullable[LocalDate] and
      (__ \ "interestEndDate").readNullable[LocalDate] and
      (__ \ "latePaymentInterestAmount").readNullable[BigDecimal] and
      (__ \ "lpiWithDunningLock").readNullable[BigDecimal] and
      (__ \ "paymentLotItem").readNullable[String] and
      (__ \ "paymentLot").readNullable[String] and
      (__ \ "effectiveDateOfPayment").readNullable[LocalDate] and
      (__ \ "amountCodedOut").readNullable[BigDecimal] and
      (__ \ "documentDueDate").readNullable[LocalDate] and
      (__ \ "poaRelevantAmount").readNullable[BigDecimal]
    )(DocumentDetail.apply _)
}

object FinancialDetail {
  implicit val format: Format[FinancialDetail] = Json.format[FinancialDetail]
}

object BalanceDetails {
  implicit val writes: Writes[BalanceDetails] = Json.writes[BalanceDetails]
  implicit val reads: Reads[BalanceDetails] = Json.reads[BalanceDetails]
}

object FinancialDetailsModel {
  implicit val format: Format[FinancialDetailsModel] = Json.format[FinancialDetailsModel]
}