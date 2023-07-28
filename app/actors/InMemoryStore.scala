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
import play.api.libs.functional.syntax.{toFunctionalBuilderOps, unlift}
import play.api.libs.json.{JsPath, Reads, Writes}
import scala.collection.mutable
import scala.io.Source

object InMemoryStore {
  def props = Props[InMemoryStore]

  case class AddDocument(document: DataModel)

  case class RemoveById(url: String)

  case class RemoveAll()

  case class Find(id: String)

  case object OK

  case class Error(error: String)

  case object LoadFromFile
}

case class KeyValuePair2(key: String, value: DataModel)
object KeyValuePair2 {
  implicit val keyValuePair2Writes: Writes[KeyValuePair2] = (
    (JsPath \ "key").write[String] and
      (JsPath \ "value").write[DataModel]
    )(unlift(KeyValuePair2.unapply))

  implicit val keyValuePair2Reads: Reads[KeyValuePair2] = (
    (JsPath \ "key").read[String] and
      (JsPath \ "value").read[DataModel]
    )(KeyValuePair2.apply _)
}

class InMemoryStore extends Actor {
  import InMemoryStore._

  var store: mutable.Map[String, DataModel] = mutable.Map[String, DataModel]()
  val inMemoryFile = "inMemoryStore.json"

  //this.self ! LoadFromFile

  def receive = {
    case LoadFromFile =>

//      store.clear()
//      val jsonAsString = Source.fromFile(inMemoryFile).getLines.toList.mkString("")
//      val json  = Json.parse(jsonAsString)
//      val list = json.as[List[KeyValuePair2]]
//      list.foreach(kv => {
//        store = store +=  (kv.key -> kv.value)
//      })
//      println(s"Load complete: ${store.size}")

    case AddDocument(document: DataModel) =>
      val key = document._id.hashCode.toString
      println(s"Adding document: ${document._id} - ${key} - ")
      if (!store.contains(key)) {
        store = store += (key -> document)
      }
      println(s"Count: ${store.size}")
      sender() ! OK

// Simple example: How to save inMemory object to file
//      if (store.size == 824) {
//        val objectToSave = store.toList.map(kv => KeyValuePair(kv._1, kv._2) )
//        val json = Json.toJson(objectToSave)
//        new PrintWriter("inMemoryStore.json") { write(json.toString()); close }
//      }
//      sender() ! OK

    case Find(id: String) =>
      val key = id.hashCode.toString
      println(s"Extracting document: ${id} - $key")
      //println(s"Data: ${store(key)}")
      if (store.contains(key)) {
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
  }


}