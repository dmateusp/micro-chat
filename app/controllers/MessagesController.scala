package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import utils._

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class MessagesController @Inject()(auth: Authentication) extends Controller {

  def getAll = auth.AuthenticatedAction { implicit request =>
    Ok("<p>Heee</p>")
  }

}
