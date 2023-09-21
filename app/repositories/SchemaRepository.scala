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

package repositories

import models.SchemaModel
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.result.{DeleteResult, InsertOneResult}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SchemaRepository @Inject()(repository: SchemaRepositoryBase) {

  def findById(schemaId: String): Future[SchemaModel] =
    repository.collection.find(equal("_id", schemaId)).head()

  def removeById(schemaId: String): Future[DeleteResult] =
    repository.collection.deleteOne(equal("_id", schemaId)).toFuture()

  def removeAll(): Future[DeleteResult] = repository.collection.deleteMany(empty()).toFuture()

  def addEntry(document: SchemaModel)(implicit ec:ExecutionContext): Future[InsertOneResult] = {
    repository.collection.deleteOne(
      equal("_id", document._id)
    ).toFuture() flatMap (_ => {
      repository.collection.insertOne(document).toFuture()
    })
  }

}
