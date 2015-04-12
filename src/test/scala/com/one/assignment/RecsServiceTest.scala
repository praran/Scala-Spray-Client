package com.one.assignment

import org.specs2.mock.Mockito
import org.scalatest.{FunSpec, FunSuite}
import org.mockito.Matchers
import spray.http.MediaType
import scala.concurrent.Future
import spray.json._
import org.specs2.mutable.Specification
import org.scalatest.mock.MockitoSugar


/**
 * Created by Pradeep Muralidharan.
 */
class RecsServiceTest extends FunSpec with Mockito with RecsJsonMarshaller {

  import scala.concurrent.ExecutionContext.Implicits.global
  import RecsJsonProtocol._

  val recsClient = mock[RecsEngineRestClient]
  val service = new RecServiceClass(recsClient)

  val jsonString = """[{
  "recommendations": [{
    "uuid": "uuid1",
    "start": 5,
    "end": 10
  }],
  "expiry": 1416261596578
}, {
  "recommendations": [{
    "uuid": "uuid2",
    "start": 5,
    "end": 10
  }],
  "expiry": 1416265196578
}, {
  "recommendations": [{
    "uuid": "uuid3",
    "start": 5,
    "end": 10
  }],
  "expiry": 1416268796578
}]"""


  describe("When the rest client throws an exception") {
    it("the service should throw a runtime exception ") {
      val successFuture = Future.failed(new Exception)
      recsClient.getRequest(Matchers.any[String])(Matchers.any[MediaType]) returns (successFuture)
      val thrown = intercept[Exception] {
        service.getRecommendations("test")
      }
    }
  }

  describe("When the time out exceeds") {
    it("the service should throw a runtime exception ") {
      import scala.concurrent.duration._
      // init
      val recommendation1 = Recommendation("uuid1", 5L, 10L)
      service.DEFAULT_TIME_TO_WAIT = 1 seconds
      val successFuture = Future.successful(recommendation1 :: Nil)
      val sf = Future {
        Thread sleep (10000)
        recommendation1 :: Nil
      }
      recsClient.getRequest(Matchers.any[String])(Matchers.any[MediaType]) returns (sf)
      val thrown = intercept[Exception] {
        service.getRecommendations("test")
      }
    }
  }

  describe("When empty recommendations received from recs client"){
    it("the service should return empty string"){
      // init
      val successFuture = Future.successful( Nil)

      // behaviour
      recsClient.getRequest(Matchers.any[String])(Matchers.any[MediaType]) returns (successFuture)
      // sut
      val recs: String = service.getRecommendations("test")
      val jRecommendations = recs.parseJson.convertTo[List[JRecommendation]]
      // assert get all recommendations with uuids initially passed are present
       jRecommendations.foreach(x => assert(x.recommendations.isEmpty))
      }
  }

  describe("When recommendation received from recs client"){
    it("the service should return recommendations"){
      // init
      val recommendation1 = Recommendation("uuid1", 5L, 10L)
      val recommendation2 = Recommendation("uuid2", 5L, 10L)
      val recommendation3 = Recommendation("uuid3", 5L, 10L)

      val successFuture = Future.successful(recommendation1 :: Nil)
      val successFuture2 = Future.successful(recommendation2 :: Nil)
      val successFuture3 = Future.successful(recommendation3 :: Nil)
      // behaviour
      recsClient.getRequest(Matchers.any[String])(Matchers.any[MediaType]) returns (successFuture) thenReturn (successFuture2) thenReturn (successFuture3)
      // sut
      val recs: String = service.getRecommendations("test")
      val jRecommendations = recs.parseJson.convertTo[List[JRecommendation]]
      // assert get all recommendations with uuids initially passed are present
      assert(jRecommendations.map(jr => jr.recommendations(0).uuid).equals(List[String]("uuid1", "uuid2", "uuid3")))
    }
  }

  describe("when recs client returns valid recommendations"){
    it("should contain valid json response from service"){
      // init
      val recommendation1 = Recommendation("uuid1", 5L, 10L)
      val recommendation2 = Recommendation("uuid2", 5L, 10L)
      val recommendation3 = Recommendation("uuid3", 5L, 10L)

      val successFuture = Future.successful(recommendation1 :: Nil)
      val successFuture2 = Future.successful(recommendation2 :: Nil)
      val successFuture3 = Future.successful(recommendation3 :: Nil)
      // behaviour
      recsClient.getRequest(Matchers.any[String])(Matchers.any[MediaType]) returns (successFuture) thenReturn (successFuture2) thenReturn (successFuture3)
      // sut
      val recs: String = service.getRecommendations("test")

      val result = recs.parseJson.convertTo[List[JRecommendation]]
      val expected1 = jsonString.parseJson.convertTo[List[JRecommendation]]

      // assert get all recommendations with uuids initially passed are present
      assert(result.map(jr => jr.recommendations(0).uuid).equals(List[String]("uuid1", "uuid2", "uuid3")))
    }
  }


}
