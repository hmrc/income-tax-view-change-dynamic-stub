package helpers

import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, GivenWhenThen, TestSuite}
import org.scalatest.concurrent.{Eventually, IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import testUtils.UnitSpec

trait ComponentSpecBase extends TestSuite with UnitSpec with GivenWhenThen
  with GuiceOneServerPerSuite with ScalaFutures with IntegrationPatience with Matchers
  with BeforeAndAfterEach with BeforeAndAfterAll with Eventually {

}
