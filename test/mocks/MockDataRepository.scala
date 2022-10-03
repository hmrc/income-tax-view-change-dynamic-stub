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

import models.DataModel
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{reset, when}
import org.mockito.stubbing.OngoingStubbing
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}
import org.scalatestplus.mockito.MockitoSugar
import repositories.DataRepository
import testUtils.TestSupport

import scala.concurrent.Future

trait MockDataRepository extends TestSupport with MockitoSugar {

  lazy val mockDataRepository: DataRepository = mock[DataRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockDataRepository)
  }

  def mockAddEntry(document: DataModel)(response: InsertOneResult): OngoingStubbing[Future[InsertOneResult]] = {
    when(mockDataRepository.addEntry(ArgumentMatchers.eq(document))).thenReturn(Future.successful(response))
  }

  def mockRemoveById(url: String)(response: DeleteResult): OngoingStubbing[Future[DeleteResult]] = {
    when(mockDataRepository.removeById(ArgumentMatchers.eq(url))).thenReturn(Future.successful(response))
  }

  def mockRemoveAll()(response: DeleteResult): OngoingStubbing[Future[DeleteResult]] = {
    when(mockDataRepository.removeAll()).thenReturn(Future.successful(response))
  }

  def mockFind(response: Option[DataModel]): OngoingStubbing[Future[Option[DataModel]]] = {
    when(mockDataRepository.find(ArgumentMatchers.any())).thenReturn(Future.successful(response))
  }

}