package controllers

import actors.WSManager.Message
import actors.{WSManager, WebSocketActor}
import org.apache.pekko.NotUsed
import org.apache.pekko.actor.{ActorSystem, Props}
import org.apache.pekko.stream.Materializer

import javax.inject._
import play.api._
import play.api.mvc._
import org.apache.pekko.stream.scaladsl._
import play.api.libs.json.JsPath.\
import play.api.libs.json.{JsNull, JsObject, JsString, Json}
import play.api.libs.streams.ActorFlow

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(val controllerComponents: ControllerComponents)(implicit system: ActorSystem, mat: Materializer) extends BaseController {
  var points1 = 0
  var points2 = 0
  var sets1 = 0
  var sets2 = 0
  var name1 = "name1"
  var name2 = "name2"
  var stage = "Round 1"
  var ws: Source[String, NotUsed] = null
  val manager = system.actorOf(Props[WSManager], "Manager")

  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index(routes.HomeController.socket.webSocketURL()))
  }

  def controller() = Action {implicit request: Request[AnyContent] =>
    Ok(views.html.controller(name1, name2, stage, points1, points2, sets1, sets2))
  }

  def getJson = Json.obj(
    "points1" -> points1,
    "points2" -> points2,
    "sets1" -> sets1,
    "sets2" -> sets2,
    "name1" -> name1,
    "name2" -> name2,
    "stage" -> stage
  )

  def addPoint(player: Int) = Action {
    if(player == 1) points1 += 1
    else points2 += 1
    if(points1 >= 11 && points1-points2 >=2) {
      points1 = 0
      points2 = 0
      sets1 += 1
    }
    if (points2 >= 11 && points2 - points1 >= 2) {
      points1 = 0
      points2 = 0
      sets2 += 1
    }
    manager ! Message(Json.stringify(getJson))
    Redirect(routes.HomeController.controller())
  }

  def addSet(player: Int) = Action {
    if (player == 1) sets1 += 1
    else sets2 += 1
    manager ! Message(Json.stringify(getJson))
    Redirect(routes.HomeController.controller())
  }

  def subtractSet(player: Int) = Action {
    if (player == 1 && sets1 > 0) sets1 -= 1
    else if(sets2 > 0) sets2 -= 1
    manager ! Message(Json.stringify(getJson))
    Redirect(routes.HomeController.controller())
  }

  def subtractPoint(player: Int) = Action {
    if (player == 1 && points1 > 0) points1 -= 1
    else if (points2 > 0) points2 -= 1
    manager ! Message(Json.stringify(getJson))
    Redirect(routes.HomeController.controller())
  }

  def reset() = Action {
    points1 = 0
    points2 = 0
    sets1 = 0
    sets2 = 0
    manager ! Message(Json.stringify(getJson))
    Redirect(routes.HomeController.controller())
  }

  def setName(player: Int, name: String) = Action {
    if(player == 1) name1 = name
    else name2 = name
    manager ! Message(Json.stringify(getJson))
    Redirect(routes.HomeController.controller())
  }

  def setStage(stage: String) = Action {
    this.stage = stage
    manager ! Message(Json.stringify(getJson))
    Redirect(routes.HomeController.controller())
  }

  def socket = WebSocket.accept[String, String] { request =>
    val ws = ActorFlow.actorRef { out => WebSocketActor.props(out, manager) }
    manager ! Message(Json.stringify(getJson))
    ws
  }
}
