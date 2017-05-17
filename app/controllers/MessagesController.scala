package controllers

import javax.inject._
import play.api._
import play.api.mvc._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class MessagesController @Inject() extends Controller {

  def getAll = Action { implicit request =>
    Ok("<p>Heee</p>")
  }
}
