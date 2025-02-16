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

import com.google.inject.Singleton
import models.{DataModel, SchemaModel}
import org.mongodb.scala.model.{IndexModel, Indexes}
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository

import javax.inject.Inject
import scala.concurrent.ExecutionContext

@Singleton
class DataRepositoryBase @Inject() (implicit mongo: MongoComponent, val ec: ExecutionContext)
    extends PlayMongoRepository[DataModel](
      mongoComponent = mongo,
      collectionName = "data",
      domainFormat = DataModel.formats,
      indexes = Seq(
        IndexModel(
          Indexes.ascending("method")
        )
      ),
      replaceIndexes = true
    )

class SchemaRepositoryBase @Inject() (implicit mongo: MongoComponent, val ec: ExecutionContext)
    extends PlayMongoRepository[SchemaModel](
      mongoComponent = mongo,
      collectionName = "schemas",
      domainFormat = SchemaModel.formats,
      indexes = Seq()
    )
