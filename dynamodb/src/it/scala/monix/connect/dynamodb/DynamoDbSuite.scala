package monix.connect.dynamodb

import java.lang.Thread.sleep

import monix.eval.Task
import monix.execution.Scheduler.Implicits.global
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterAll
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpecLike
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.model._

import scala.collection.JavaConverters._
import scala.compat.java8.FutureConverters._
import scala.concurrent.Await
import scala.concurrent.duration._

class DynamoDbSuite extends AnyFlatSpec with Matchers with DynamoDbFixture with BeforeAndAfterAll {

  private val client: DynamoDbAsyncClient = DynamoDbClient()
  private val dynamoDb: DynamoDb = DynamoDb.createUnsafe(client)

  s"${DynamoDbOp}" should "implement batchGetItems" in {}

  it should "putItem" in {
    //given
    val city = Gen.nonEmptyListOf(Gen.alphaChar).sample.get.mkString
    val citizenId = genCitizenId.sample.get
    val debt = Gen.choose(0, 10000).sample.get
    val newItem: Map[String, AttributeValue] = item(city, citizenId, debt)
    val request: PutItemRequest = putItemRequest(tableName, city, citizenId, debt)

    //when
    val t: Task[PutItemResponse] = dynamoDb.putItem(tableName, newItem)

    //then
    t.runSyncUnsafe() shouldBe a[PutItemResponse]
    val getResponse: GetItemResponse =
      Task.from(toScala(client.getItem(getItemRequest(tableName, city, citizenId)))).runSyncUnsafe()
    getResponse.item().values().asScala.head.n().toDouble shouldBe debt
  }

  override def beforeAll(): Unit = {
    val f = dynamoDb.createTable(tableName, keySchema, tableDefinition).runToFuture
    Await.ready(f, 1.second)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
    super.afterAll()
  }
}