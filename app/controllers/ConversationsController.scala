package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.mvc.Results._
import utils._
import play.api.libs.json._
import models._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class ConversationsController @Inject()(auth: Authentication) extends Controller {

  def index = Action { implicit request =>
    Ok("test")
  }

  def create = auth.AuthenticatedAction { implicit request =>
    val jsonBody: Option[JsValue] = request.body.asJson
    jsonBody match {
      case Some(json) => {
        val newConversation: Conversation = Conversation((json \ "participants").as[Vector[String]], (json \ "admins").as[Vector[String]])
        val result = newConversation.save(ConversationKey((json \ "createdBy").as[String], (json \ "conversationName").as[String]))
        if(result) {
          Ok("Conversation created!")
        } else {
          Ok("Conversation already exists")
        }
      }
      case None => BadRequest
    }
  }
}
