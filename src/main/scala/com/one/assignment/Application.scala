package com.one.assignment

import akka.actor.{Props, ActorSystem}
import akka.util.Timeout
import spray.routing.SimpleRoutingApp
import scala.concurrent.Await
import scala.xml.{Elem, XML}
import java.io.{ByteArrayInputStream, InputStreamReader}

object Application extends App with SimpleRoutingApp {

  implicit val system = ActorSystem("recs")

  // can be bootstrapped or injected
  val restClient = new RecsEngineRestClient
  val service = new RecServiceClass(restClient)


  startServer(interface = "localhost", port = 8090) {
    path("personalised" / Segment) {
      subscriber =>
        get {
          complete {

            <h1>Recommendations for
              {subscriber}
              in json format</h1>
            service.getRecommendations(subscriber)

          }
        }
    }
  }
}
