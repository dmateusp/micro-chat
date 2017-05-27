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
class MessagesController @Inject()(auth: Authentication) extends Controller {

  def getAll = auth.AuthenticatedAction { implicit request =>
    Ok("<p>Heee</p>")
  }
  def create = auth.AuthenticatedAction { implicit request =>
    val jsonBody: Option[JsValue] = request.body.asJson
    jsonBody match {
      case Some(json) => {
        try{
          val message: Message = Message((json \ "sender").as[String], (json \ "body").as[String])
          val conversationKey: ConversationKey = (json \ "conversation").as[ConversationKey]
          val result : Boolean = message.save(conversationKey)
          if(result) Ok("success") else Ok("error")

        } catch {
           case e: JsResultException => Ok(JsResultExceptionJson.toJson(e))
           case x: Throwable => Ok(x.toString())
        }
      }
      case None => Ok("ohhh")
    }
  }
}
