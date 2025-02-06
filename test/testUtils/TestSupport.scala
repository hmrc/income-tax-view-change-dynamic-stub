/*
 * Copyright 2023 HM Revenue & Customs
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

package testUtils

import com.mongodb.client.result.{DeleteResult, InsertOneResult, UpdateResult}
import com.typesafe.config.Config
import org.bson.BsonBoolean
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.test.Helpers.stubControllerComponents
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext

trait TestSupport
    extends UnitSpec
    with GuiceOneServerPerSuite
    with MockitoSugar
    with BeforeAndAfterAll
    with BeforeAndAfterEach
    with MaterializerSupport {

  implicit val ec:            ExecutionContext = stubControllerComponents().executionContext
  implicit val headerCarrier: HeaderCarrier    = HeaderCarrier()
  implicit val config:        Config           = app.configuration.underlying

  val successUpdateResult    = UpdateResult.acknowledged(0, 0, BsonBoolean.TRUE)
  val failedUpdateResult     = UpdateResult.unacknowledged()
  val successInsertOneResult = InsertOneResult.acknowledged(BsonBoolean.TRUE)
  val failedInsertOneResult  = InsertOneResult.unacknowledged()
  val successDeleteResult: DeleteResult = DeleteResult.acknowledged(0)
  val failedDeleteResult:  DeleteResult = DeleteResult.unacknowledged()

}
