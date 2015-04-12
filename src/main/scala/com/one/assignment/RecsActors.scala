package com.one.assignment

import akka.routing.RoundRobinPool
import akka.util.Timeout
import spray.http.MediaTypes._
import spray.json.{JsonFormat, DefaultJsonProtocol}
import scala.concurrent.{Awaitable, Future, Await}
import scala.concurrent.duration._
import spray.http.{HttpCharsets, HttpEntity, HttpResponse}
import akka.actor.{Props, ActorSystem, Actor}
import spray.httpx.unmarshalling.Unmarshaller
import scala.xml.{Elem, NodeSeq, XML, Node}
import java.io.{InputStreamReader, ByteArrayInputStream}
import scala.util.{Failure, Success}


/**
 * Created by Pradeep Muralidharan.
 */

/** *
  *
  *  THESE CLASS ARE  DEPRECATED
  *
  *  INITIALLY TRIED TO USE AKKA ACTORS GUESS ITS AN OVERKILL
  */


  case class GetRecommendation(num: Long, start: Long, end: Long, subscriber: String);
  case class RecForSub(sub:String);
  case class Result(r: JRecommendation);
  case class FinalResult(recommendations: List[JRecommendation]);
  case class JRecommendation(recommendations:List[Recommendation], expiry:Long);

  object RecsJsonProtocol2 extends DefaultJsonProtocol {
    implicit val recommendationFormat:JsonFormat[Recommendation] = jsonFormat3(Recommendation)
    implicit val jRecommendationFormat:JsonFormat[JRecommendation] = jsonFormat2(JRecommendation)
  }

  class RecsAggregator extends Actor{
    import spray.json._
    import RecsJsonProtocol2._

    override def receive = {
      case FinalResult(r) => println(r.toJson.prettyPrint )
    }

  }



class RecsMaster extends Actor {
 implicit val system = ActorSystem("recs")

 val workerRouter =   system.actorOf(Props[RecsActor].withRouter(RoundRobinPool(5)),name="workrouter")
 val recsAggregator = system.actorOf(Props[RecsAggregator], name = "recsAggregator")

 val noOfReqs = 3
 var noOfRes:Int =0
 var result = List[JRecommendation]()

 override  def receive ={

   case RecForSub(s)                   =>  for(msg <- generateMessage(s)(noOfReqs)) workerRouter! msg
   case Result(r)                      =>  result = r :: result
                                           noOfRes+=1
                                           if(noOfRes == noOfReqs) {
                                             //recsAggregator ! FinalResult(result)
                                             println("final results "+result)
                                            // context.stop(self)
                                           }


 }


 def generateMessage(subscriber:String)( numOfReqs:Int):List[GetRecommendation] = {
   val time = System.currentTimeMillis();
   // generate
   (0 until numOfReqs) map( x =>GetRecommendation(5, time+(60 minutes).*(x).toMillis, time+(60 minutes).*(x).toMillis+(60 minutes).toMillis,subscriber)) toList
 }

}

class RecsActor extends Actor {

 import system.dispatcher

 implicit val system = ActorSystem("recs")
 implicit val BASE_URL: String = "http://localhost:8080/recs/personalised".toString
 implicit val mediaType = `application/xml`
 implicit val timeToWait = 10 seconds


 override def receive = {
   case GetRecommendation(num, start, end, sub) =>
                                 sender ! Result(JRecommendation(getRecommendations(num,start,end,sub), end))

 }

 def getRecommendations(num: Long, start: Long, end: Long, sub: String): List[Recommendation] = {
   val recs =  new RecsEngineRestClient().getRequest(constructUrl(num, start, end, sub))(`application/xml`)
    Await.result(recs, 20 seconds)
 }



 private def constructUrl(num: Long, start: Long, end: Long, sub: String) =
   s"$BASE_URL?num=$num&start=$start&end=$end&subscriber=$sub"


}

