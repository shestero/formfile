import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.ExceptionHandler
import akka.stream.scaladsl.Source
import akka.util.ByteString

import java.io.PrintWriter
import scala.io.StdIn
import scala.concurrent.ExecutionContextExecutor
import scala.concurrent.duration.DurationInt

object Main extends App {

  val app = "akka-demo"
  val host = "0.0.0.0"
  val port = 8888


  implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, app)
  implicit val executionContext: ExecutionContextExecutor = system.executionContext
  system.whenTerminated.foreach { t => println(s"$app\tterminated; " + t) }

  val routes = handleExceptions(ExceptionHandler {
    case e: Exception =>
      println(s"Catch error: " + e.getMessage)
      e.printStackTrace(new PrintWriter(System.out))
      complete("Error")
  }) {
    (post & path("post")) {
      withoutRequestTimeout {
        withoutSizeLimit {
          FieldsAndFiles.withFormContent { content =>
            val source: Source[ByteString, NotUsed] =
              Source.future(content.map(_.toString)).keepAlive(5.seconds, () => "...\n").map(ByteString(_))
            complete(HttpEntity(ContentTypes.`text/plain(UTF-8)`, source))
          }
        }
      }
    } ~
    pathEndOrSingleSlash {
      getFromFile("static/index.html")
    } ~
    pathPrefix("") { // not used now; but you may store there something like CSS etc
      get{
        getFromDirectory("static")
      }
    }
  }


  val binding = Http().newServerAt(host, port).bind(routes)
  binding.map { _ =>
    println(s"Successfully started on $host:$port")
  } recover { case ex =>
    println("Failed to start the server due to: " + ex.getMessage)
  }
  println(s"Server online at http://$host:$port/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  binding
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done
}
