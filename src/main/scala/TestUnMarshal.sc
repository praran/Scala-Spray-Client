import com.sun.xml.internal.ws.encoding.xml.XMLCodec
import java.io.{ByteArrayInputStream, InputStreamReader}
import scala.concurrent.{Await, Future}
import scala.xml.{Elem, Node, NodeSeq, XML}
/**
 * Created by Pradeep Muralidharan.
 */
object test extends App {
  import scala.concurrent.duration._
  import scala.concurrent.ExecutionContext.Implicits.global

  get()
   def get():Unit = {

     val f = for {
       x <- 0 until 10
       a <- Future(10 / 2) // 10 / 2 = 5
       b <- Future(a + 1) //  5 + 1 = 6
       c <- Future(a - 1) //  5 - 1 = 4
       if c > 3 // Future.filter
     } yield b * c //  6 * 4 = 24

     // Note that the execution of futures a, b, and c
     // are not done in parallel.

     val result = Await.result(f, 1 second)

     println(result)

   }

}



