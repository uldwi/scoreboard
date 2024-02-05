package actors

import actors.WebSocketActor.SendMessage
import org.apache.pekko.actor.{Actor, ActorRef}

class WSManager extends Actor{
  import WSManager._

  private var sockets = List.empty[ActorRef]

  def receive = {
    case NewSocket(socket) => {
      println("new Socket")
      sockets ::= socket
    }
    case Message(msg) => {
      println("manager: " + msg)
      sockets.foreach(_ ! SendMessage(msg))
    }
  }

}

object WSManager {
  case class NewSocket(socket: ActorRef)
  case class Message(msg: String)
}
