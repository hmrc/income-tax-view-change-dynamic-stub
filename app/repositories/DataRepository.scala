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

import models.{CustomUserModel, DataModel}
import org.mongodb.scala.Document
import org.mongodb.scala.bson.BsonDocument
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.{Filters, ReplaceOptions, UpdateOptions, Updates}
import org.mongodb.scala.result.{DeleteResult, UpdateResult}
import scala.jdk.CollectionConverters._

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class DataRepository @Inject() (repository: DataRepositoryBase) {

  def removeAll(): Future[DeleteResult] = repository.collection.deleteMany(empty()).toFuture()

  def removeById(url: String): Future[DeleteResult] = repository.collection.deleteOne(equal("_id", url)).toFuture()

  def addEntry(document: DataModel): Future[UpdateResult] =
    repository.collection
      .replaceOne(
        equal("_id", document._id),
        document,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()

  def find(query: Bson*): Future[Option[DataModel]] = {
    val finalQuery = if (query.isEmpty) empty() else and(query: _*)
    repository.collection.find(finalQuery).headOption()
  }

  def replaceOne(url: String, updatedFile: DataModel): Future[UpdateResult] = {
    repository.collection
      .replaceOne(
        filter = Filters.equal("_id", url),
        replacement = updatedFile,
        options = new ReplaceOptions().upsert(true)
      )
      .toFuture()
  }

  def updateOneById(url: String, userModel: CustomUserModel)(implicit ec: ExecutionContext): Future[UpdateResult] = {

    val filter  = Filters.equal("_id", url)

    val updates = Updates.combine(
      Updates.set("response.success.taxPayerDisplayResponse.channel", userModel.channel)
    )

    repository.collection
      .updateOne(filter, updates)
      .toFuture()
  }

  def clearAndReplace(url: String, arrayField: String, newArray: Seq[Document]): Future[UpdateResult] = {
    val filter = Filters.equal("_id", url)
    val bsonDocs: Seq[BsonDocument] = newArray.map(_.toBsonDocument)
    val updates = Updates.set(arrayField, bsonDocs)


    repository.collection
      .updateOne(filter, updates, UpdateOptions().upsert(true))
      .toFuture()
  }
}
