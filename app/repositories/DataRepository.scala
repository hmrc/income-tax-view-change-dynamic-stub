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

import actors.InMemoryStore
import actors.InMemoryStore.{AddDocument, RemoveAll, RemoveById, Find}
import akka.actor.ActorSystem
import akka.pattern.ask
import akka.util.Timeout
import com.mongodb.client.result.{DeleteResult, InsertOneResult}
import models.DataModel
import org.mongodb.scala.bson.conversions.Bson

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration.DurationInt

@Singleton
class DataRepository @Inject()(system: ActorSystem, repository: DataRepositoryBase) {

  private val inMemoryStore = system.actorOf(InMemoryStore.props, "inMemoryStore-actor")
  implicit val timeout: Timeout = 5.seconds

  def removeAll(): Future[Any] = {
    ((inMemoryStore ? RemoveAll()))
  }
  //repository.collection.deleteMany(empty()).toFuture()

  def removeById(url: String): Future[Any] =
    (inMemoryStore ? RemoveById(url))
  //repository.collection.deleteOne(equal("_id", url)).toFuture()

  def addEntry(document: DataModel): Future[Any] =
    (inMemoryStore ? AddDocument(document))
  //repository.collection.insertOne(document).toFuture()

  def find(query: Bson*): Future[Option[DataModel]] = {
    val documentId =
      query.headOption.get.toString
        .replace("Filter{fieldName='_id', value=/", "")
        .replace("}", "")
    ((inMemoryStore ? Find(s"/$documentId"))).mapTo[Any].map{
      case dataModel: DataModel =>
        Some(dataModel)
      case _ => None
    }
    //val finalQuery = if (query.isEmpty) empty() else and(query: _*)
    //repository.collection.find(finalQuery).headOption()
  }

}