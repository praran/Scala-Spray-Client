package com.one.assignment

import spray.http.MediaTypes._
import spray.json._
import spray.json.DefaultJsonProtocol
import scala.concurrent.{Promise, Future, Await}
import scala.concurrent.duration._
import scala.util.{Failure, Success}

/**
 * Created by Pradeep Muralidharan.
 */


trait RecsJsonMarshaller {

  case class JRecommendation(recommendations: List[Recommendation], expiry: Long)

  // Implicit objects for json formatting
  object RecsJsonProtocol extends DefaultJsonProtocol {
    implicit val recommendationFormat: JsonFormat[Recommendation] = jsonFormat3(Recommendation)
    implicit val jRecommendationFormat: JsonFormat[JRecommendation] = jsonFormat2(JRecommendation)
  }

}

trait RecService {
  // default variables
  implicit val BASE_URL: String = "http://localhost:8080/recs/personalised"
  var DEFAULT_TIME_TO_WAIT = 10 seconds
  var DEFAULT_NUM_SLOTS = 3
  var DEFAULT_SLOT_DURATION = 1 hour
  var DEFAULT_NUM_REQUESTS = 5


  /**
   * Gets Recommendations for the subscriber
   * @param subscriber
   * @return
   */
  def getRecommendations(subscriber: String): String
}


class RecServiceClass(client: RecsEngineRestClient) extends RecService with RecsJsonMarshaller {

  import RecsJsonProtocol._
  import scala.concurrent.ExecutionContext.Implicits._


  /**
   * Get recommendations for subscriber
   * @param subscriber
   * @return
   */
  def getRecommendations(subscriber: String): String = {

    val time: Long = System.currentTimeMillis();
    // get the recommendations
    val res = for {
      x <- 0 until DEFAULT_NUM_SLOTS
      start = time + (DEFAULT_SLOT_DURATION * x  toMillis)
      end = start + DEFAULT_SLOT_DURATION.toMillis
      r = getRecommendations(DEFAULT_NUM_REQUESTS, start, end, subscriber)
    } yield r

    // List of Recommendations of future
    val recommendations = Future sequence res.toList

    // wait for 10 seconds before giving the results
    val result = Await result(recommendations, DEFAULT_TIME_TO_WAIT)

    // Return json string
    result.toJson.prettyPrint

  }

  /**
   * get recommendations based on the parameters
   * @param num
   * @param start
   * @param end
   * @param sub
   * @return
   */
  def getRecommendations(num: Long, start: Long, end: Long, sub: String): Future[JRecommendation] = {
    /*val recs =     RecsEngineRestClientObj.getRequest(constructUrl(num, start, end, sub))(`application/xml`)
     Await.result(recs, 5 seconds)*/
    val result = Promise[JRecommendation]
    // val x = new RecsEngineRestClientClass
    client.getRequest(constructUrl(num, start, end, sub))(`application/xml`) onComplete {
      case Success(r) => result.success(JRecommendation(r, end))
      case Failure(ex) => result.failure(new Exception(s"Could not get result ${num} ${start} ${end} ${sub}"))
    }
    result.future
  }

  /**
   * Construct the url
   * @param num
   * @param start
   * @param end
   * @param sub
   * @return
   */
  private def constructUrl(num: Long, start: Long, end: Long, sub: String) =
    s"$BASE_URL?num=$num&start=$start&end=$end&subscriber=$sub"

}
