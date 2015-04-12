package com.one.assignment

import akka.actor.ActorSystem
import spray.httpx.ResponseTransformation
import scala.concurrent.Future
import spray.http._
import spray.client.pipelining._
import spray.http.HttpHeaders.Accept
import spray.http.MediaTypes._


/**
 * Created by Pradeep Muralidharan.
 */

/**
 * Case class representing the recommendation from Recs Engine
 * @param uuid
 * @param start
 * @param end
 */
case class Recommendation(uuid: String, start: Long, end: Long)

trait RecsRestClientTrait {

  import scala.xml.{Elem, XML}
  import java.io.{InputStreamReader, ByteArrayInputStream}
  import spray.http.{HttpEntity}
  import spray.http.MediaTypes._
  import spray.httpx.unmarshalling._

  /**
   *  default Unmarshaller for Recs Engine xml response
   */
  implicit val recsUnMarshaller =
    Unmarshaller[List[Recommendation]](`text/xml`, `application/xml`) {
      case HttpEntity.NonEmpty(contentType, data) =>
        val parser = XML.parser
        val elem: Elem = XML.withSAXParser(parser).load(new InputStreamReader(new ByteArrayInputStream(data.toByteArray), contentType.charset.nioCharset))
        elemToRecommendation(elem)
      case HttpEntity.Empty => List[Recommendation]();
    }

  /**
   * Simple xml parser to parse elem to Recommendation objects
   * @param elem
   * @return
   */
  def elemToRecommendation(elem: Elem): List[Recommendation] = {
    elem \ ("recommendations") map (x => Recommendation((x \ "uuid").text, (x \ "start").text.toLong, (x \ "end").text.toLong)) toList
  }

  /**
   * Recs client for get request
   * @param url
   * @param mediaType
   * @return
   */
  def getRequest (url: String)(mediaType: MediaType) : Future[List[Recommendation]]
}


/**
 * Rest client to request and marshal response from Recs engine to Recommendations
 */
class RecsEngineRestClient  extends RecsRestClientTrait{
  // import required for implicits
  import system.dispatcher
  import spray.httpx.unmarshalling.Unmarshaller

  implicit val system = ActorSystem("recs")
  implicit val mediaType = `application/xml`

  /**
   * sendRecieve method from trait
   * @return
   */
  def customSendReceive = sendReceive

  /**
   * Request for the give URI
   * return future of recommendations object
   * @param url
   * @param mediaType
   * @return
   */
  override def getRequest (url: String)(mediaType: MediaType) : Future[List[Recommendation]] = {
    val pipeline = (addHeader(Accept(mediaType))
      ~> customSendReceive
      ~> unmarshal[List[Recommendation]])
    pipeline {
      Get(url)
    }
  }


}
