package utils

import org.mongodb.scala.bson.BsonString
import testUtils.TestSupport

import java.time.LocalDate

class BusinessDataUtilsSpec extends TestSupport {

  private val currentYear: Int = LocalDate.now().getYear


  "createBusinessData" should {
    "create business documents" when {
      "Active Sole trader is true and ceased sole trader is true" in {
        val businessData = BusinessDataUtils.createBusinessData(activeSoleTrader = true, ceasedSoleTrader = true)
        val docMap1 = businessData.head.toMap
        val docMap2 = businessData(1).toMap
        docMap1.get("incomeSourceId") shouldBe Some(BsonString("XAIS00000000001"))
        docMap2.get("incomeSourceId") shouldBe Some(BsonString("XAIS00000000002"))
        docMap2.get("cessationDate") shouldBe Some(BsonString(s"${currentYear - 1}-06-30"))
        businessData.size shouldBe 2
      }
      "Active Sole trader is true and ceased sole trader is false" in {
        val businessData = BusinessDataUtils.createBusinessData(activeSoleTrader = true, ceasedSoleTrader = false)
        businessData.size shouldBe 1
      }
      "Active Sole trader is false and ceased sole trader is true" in {
        val businessData = BusinessDataUtils.createBusinessData(activeSoleTrader = false, ceasedSoleTrader = true)
        val docMap = businessData.head.toMap
        docMap.get("cessationDate") shouldBe Some(BsonString(s"${currentYear - 1}-06-30"))
        businessData.size shouldBe 1
      }
    }
    "return an empty Seq" when {
      "active sole trader is false and ceased sole trader is false" in {
        val businessData = BusinessDataUtils.createBusinessData(activeSoleTrader = false, ceasedSoleTrader = false)
        businessData shouldBe Seq.empty
      }
    }
  }

  "createPropertyData" should {
    "create property documents" when {
      "UK property is true and foreign property is true" in {
        val propertyData = BusinessDataUtils.createPropertyData(ukProperty = true, foreignProperty = true)
        val docMap1 = propertyData.head.toMap
        val docMap2 = propertyData(1).toMap
        docMap1.get("incomeSourceType") shouldBe Some(BsonString("02"))
        docMap2.get("incomeSourceType") shouldBe Some(BsonString("03"))
        propertyData.size shouldBe 2
      }
      "UK property is true and foreign property is false" in {
        val propertyData = BusinessDataUtils.createPropertyData(ukProperty = true, foreignProperty = false)
        val docMap = propertyData.head.toMap
        docMap.get("incomeSourceType") shouldBe Some(BsonString("02"))
        propertyData.size shouldBe 1
      }
      "UK property is false and foreign property is true" in {
        val propertyData = BusinessDataUtils.createPropertyData(ukProperty = false, foreignProperty = true)
        val docMap = propertyData.head.toMap
        docMap.get("incomeSourceType") shouldBe Some(BsonString("03"))
        propertyData.size shouldBe 1
      }
    }
    "return an empty Seq" when {
      "UK property is false and foreign property is false" in {
        val propertyData = BusinessDataUtils.createPropertyData(ukProperty = false, foreignProperty = false)
        propertyData shouldBe Seq.empty
      }
    }
  }
}
