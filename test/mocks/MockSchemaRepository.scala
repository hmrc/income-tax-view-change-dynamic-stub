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

package mocks

import com.mongodb.client.result.{DeleteResult, InsertOneResult}
import models.SchemaModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito._
import org.mockito.stubbing.OngoingStubbing
import org.scalatestplus.mockito.MockitoSugar
import repositories.SchemaRepository
import testUtils.TestSupport


import scala.concurrent.Future

trait MockSchemaRepository extends TestSupport with MockitoSugar {

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSchemaRepository)
  }

  lazy val mockSchemaRepository: SchemaRepository = mock[SchemaRepository]

  def setupMockAddSchema(model: SchemaModel)(response: InsertOneResult): OngoingStubbing[Future[InsertOneResult]] =
    when(mockSchemaRepository.addEntry(ArgumentMatchers.eq(model))).thenReturn(Future.successful(response))

  def setupMockRemoveSchema(id: String)(response: DeleteResult): OngoingStubbing[Future[DeleteResult]] =
    when(mockSchemaRepository.removeById(ArgumentMatchers.eq(id))).thenReturn(Future.successful(response))

  def setupMockRemoveAllSchemas(response: DeleteResult): OngoingStubbing[Future[DeleteResult]] =
    when(mockSchemaRepository.removeAll()).thenReturn(Future.successful(response))

}
