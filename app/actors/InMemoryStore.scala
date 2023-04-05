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

package actors

import akka.actor._
import models.DataModel

import scala.collection.mutable
object InMemoryStore {
  def props = Props[InMemoryStore]
  case class AddDocument(document: DataModel)
  case class RemoveById(url: String)
  case class RemoveAll()
  case class Find(id: String)
  case object OK
  case class Error(error: String)
}


class InMemoryStore extends Actor {
  import InMemoryStore._

  var store : mutable.Map[String, DataModel] = mutable.Map[String, DataModel]()
  def receive = {
    case AddDocument(document: DataModel) =>
      val key = document._id.hashCode.toString
      println(s"Adding document: ${document._id} - ${key}")
      store = store += (key -> document)
      println(s"Size: ${store.size}")
      sender() ! OK

    case Find(id: String) =>
      val key = id.hashCode.toString
      println(s"Extracting document: ${id} - $key")
      //println(s"Data: ${store(key)}")
      if ( store.contains(key)){
        sender() ! store(key)
      } else {
        sender() ! Error(s"Key $key not found !")
      }

    case RemoveAll() =>
      store.clear()
      sender() ! OK

    case RemoveById(url) =>
      store -= url
      sender() ! OK

    case _ =>
      println(s"Message not identified ...")
  }



}