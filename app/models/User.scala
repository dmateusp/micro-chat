package models
import play.api.libs.functional.syntax._
import play.api.libs.json._
import db._
import redis.clients.jedis._
import org.sedis._
import Dress._
import play.api.libs.json.Reads._
import services._

case class User(name: String, participating: Boolean, admin: Boolean, conversation: ConversationKey){
  val toNumbers : () => UserNumbers = () => UserNumbers(name, User.boolToNumber(participating), User.boolToNumber(admin))
}
object User{
  implicit val userReads: Reads[User] = (
  (JsPath \ "name").read[String] and
  (JsPath \ "participating").read[Boolean] and
  (JsPath \ "admin").read[Boolean] and
  (JsPath \ "conversation").read[ConversationKey])(User.apply _)

  implicit val userWrites: Writes[User] = (
  (JsPath \ "name").write[String] and
  (JsPath \ "participating").write[Boolean] and
  (JsPath \ "admin").write[Boolean] and
  (JsPath \ "conversation").write[ConversationKey])(unlift(User.unapply))

}

case class UserNumbers(name: String, participating: Int, admin: Int){
  val merge: (UserNumbers) => UserNumbers = (user) => UserNumbers(name, participating + user.participating, admin + user.admin)
}
