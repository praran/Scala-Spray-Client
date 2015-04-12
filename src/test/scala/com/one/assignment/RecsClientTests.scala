package com.one.assignment

import org.specs2.mock.Mockito
import org.scalatest.{FunSpec, FunSuite}
import spray.http.{HttpEntity, StatusCode, HttpResponse, HttpRequest}
import scala.concurrent.{Await, Promise}
import spray.http.MediaTypes.`application/xml`
import scala.concurrent.duration._


/**
 * Created by Pradeep Muralidharan.
 */
class RecsClientTests extends FunSpec with Mockito {


  val responseString = "<recommendations><recommendations><uuid>1f18536a-e86f-4781-9819-7b3e7d385908</uuid><start>1415288463203</start><end>1415289998492</end></recommendations></recommendations>"


  describe("When rest client cannot make a request to the destination") {
    it("should throw exception") {
      // init
      val body = HttpEntity(`application/xml`, "".getBytes)
      val mockResponse = mock[HttpResponse]
      val mockStatus = mock[StatusCode]
      //  behaviour
      mockResponse.status returns mockStatus
      mockStatus.isSuccess returns false
      mockResponse.entity returns body
      // SUT
      val recsClient = new RecsEngineRestClient {
        override def customSendReceive = {
          (req: HttpRequest) => Promise.successful(mockResponse).future
        }
      }
      val future = recsClient.getRequest("http://localhost:8080/recs")(`application/xml`)
      // should throw exception
      val thrown = intercept[Exception] {
        val res = Await.result(future, 10 seconds)
      }

    }
  }

  describe("When invalid status code received") {
    it("rest client should return empty recommendations") {
      // init
      val body = HttpEntity(`application/xml`, "".getBytes)
      val mockResponse = mock[HttpResponse]
      val mockStatus = mock[StatusCode]
      //  behaviour
      mockResponse.status returns mockStatus
      mockStatus.isSuccess returns true
      mockResponse.entity returns body
      // SUT
      val recsClient = new RecsEngineRestClient {
        override def customSendReceive = {
          (req: HttpRequest) => Promise.successful(mockResponse).future
        }
      }
      val future = recsClient.getRequest("http://localhost:8080/recs")(`application/xml`)
      val res = Await.result(future, 10 seconds)
      // assert
      assertResult(Nil)(res)
    }
  }

  describe("When valid response received with empty body") {
    it("rest client should return empty recommendations") {
      // init
      val body = HttpEntity(`application/xml`, "")
      val mockResponse = mock[HttpResponse]
      val mockStatus = mock[StatusCode]
      //  behaviour
      mockResponse.status returns mockStatus
      mockStatus.isSuccess returns true
      mockResponse.entity returns body
      // SUT
      val recsClient = new RecsEngineRestClient {
        override def customSendReceive = {
          (req: HttpRequest) => Promise.successful(mockResponse).future
        }
      }
      val future = recsClient.getRequest("http://localhost:8080/recs")(`application/xml`)
      val res = Await.result(future, 10 seconds)
      // assert
      assertResult(Nil)(res)
    }
  }

  describe("When valid response received") {
    it("rest client should return valid recommendations") {
      // init
      val body = HttpEntity(`application/xml`, responseString.getBytes)
      val mockResponse = mock[HttpResponse]
      val mockStatus = mock[StatusCode]
      //  behaviour
      mockResponse.status returns mockStatus
      mockStatus.isSuccess returns true
      mockResponse.entity returns body
      // SUT
      val recsClient = new RecsEngineRestClient {
        override def customSendReceive = {
          (req: HttpRequest) => Promise.successful(mockResponse).future
        }
      }
      val future = recsClient.getRequest("http://localhost:8080/recs")(`application/xml`)
      val res = Await.result(future, 10 seconds)
      // assert
      assertResult(Recommendation("1f18536a-e86f-4781-9819-7b3e7d385908", 1415288463203L, 1415289998492L) :: Nil)(res)
    }
  }


}
