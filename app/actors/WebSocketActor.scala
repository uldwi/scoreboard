package actors

import org.apache.pekko.actor._
import play.api.libs.json.{JsObject, Json}

object WebSocketActor {
  def props(out: ActorRef, manager: ActorRef) = Props(new WebSocketActor(out, manager))

  case class SendMessage(msg: String)
}

class WebSocketActor(out: ActorRef, manager: ActorRef) extends Actor {
  import WebSocketActor._

  manager ! WSManager.NewSocket(self)

  def receive = {
    case msg: String =>

    case SendMessage(msg) => {
      println(msg)
      out ! msg
    }
  }
}
