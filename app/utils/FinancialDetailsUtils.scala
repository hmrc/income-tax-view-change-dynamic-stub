package utils

import models.TaxYear
import org.mongodb.scala.model.Filters.equal
import org.mongodb.scala.result
import play.api.Logger
import play.api.libs.json.JsValue
import repositories.DataRepository

import scala.concurrent.{ExecutionContext, Future}

trait FinancialDetailsUtils extends PoaUtils {

  def overwriteTotalAmount(nino: String, json: JsValue, dataRepository: DataRepository)(implicit ec: ExecutionContext): Unit = {
    (extractPoAAmount(json), extractTaxYear(json)) match {
      case (Some(amount), Some(taxYearString)) =>
        TaxYear.createTaxYearGivenTaxYearRange(taxYearString) match {
          case Some(taxYear) =>
            overrideFinancialDetails(nino, taxYear, amount, dataRepository)
          case None => Future.failed(new Exception("Failed to create tax year from request"))
        }
      case _ => Future.failed(new Exception("Could not extract poa amount or tax year from request"))
    }
  }

  private def overrideFinancialDetails(nino: String, taxYear: TaxYear, amount: BigDecimal, dataRepository: DataRepository)(implicit ec: ExecutionContext): Future[Any] = {
    val financialUrl = getFinancialDetailsUrl(nino, taxYear)
    val financialDetailsResponse = dataRepository.find(equal("_id", financialUrl))
    financialDetailsResponse.map {
      case Some(value) => value.response match {
        case Some(response) =>
          performDataChanges(response, amount, financialUrl, dataRepository)
          Logger("application").info(s"Overwrote totalAmount data for $nino with new amount $amount")
        case None =>
          Future.failed(new Exception("Could not find response in financial details 1553 data for this nino"))
      }
      case None => Future.failed(new Exception("Could not find financial details 1553 data for this nino"))
    }
  }

  private def performDataChanges(response: JsValue, amount: BigDecimal, financialUrl: String, dataRepository: DataRepository): Future[result.UpdateResult] = {
    //Create new 1553 data with totalAmount overwritten with new poa amount
    val newResponse = response.transform(transformDocDetails(amount)).getOrElse(response)
    //Overwrite existing 1553 data with the new poa amount
    dataRepository.replaceOne(url = financialUrl, updatedFile = getFinDetailsDataModel(newResponse, financialUrl))
  }

}
