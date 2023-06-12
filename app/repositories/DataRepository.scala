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

import models.DataModel
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.Filters._
import org.mongodb.scala.model.ReplaceOptions
import org.mongodb.scala.result.{DeleteResult, UpdateResult}

import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

@Singleton
class DataRepository @Inject()(repository: DataRepositoryBase) {

  def removeAll(): Future[DeleteResult] = repository.collection.deleteMany(empty()).toFuture()

  def removeById(url: String): Future[DeleteResult] = repository.collection.deleteOne(equal("_id", url)).toFuture()

  def addEntry(document: DataModel): Future[UpdateResult] = repository.collection.replaceOne(
    equal("_id", document._id), document,
    options = ReplaceOptions().upsert(true)
  ).toFuture()


  def find(query: Bson*): Future[Option[DataModel]] = {
    val finalQuery = if (query.isEmpty) empty() else and(query: _*)
    repository.collection.find(finalQuery).headOption()
  }

}
