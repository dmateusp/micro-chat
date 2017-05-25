package models
import play.api.libs.functional.syntax._
import play.api.libs.json._

case class User(name: String, participating: Boolean)
object User{
  implicit val userReads: Reads[User] = (
  (JsPath \ "name").read[String] and
  (JsPath \ "participating").read[Boolean])(User.apply _)
}
case class Conversation(participants: Vector[String], admins: Vector[String]){
  val save : () => Boolean = () => {
    println(this)
    true
  }
  val test = println("heeee")
}
