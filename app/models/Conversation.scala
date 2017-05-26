package models
import play.api.libs.functional.syntax._
import play.api.libs.json._
import db._
import redis.clients.jedis._
import org.sedis._
import Dress._
import play.api.libs.json.Reads._

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

  private val boolToNumber : (Boolean) => Int = (bool) => if(bool) 1 else -1
  private val numberToBool : (Int) => Boolean = (int) => if(int != 0) true else false

  val getConversations : (String) => Option[Vector[User]] = (name) => {
    DB.pool.withClient { client =>
      val participationInfo: Vector[User] = client.lrange(name, 0, -1).map((u: String) => Json.parse(u).as[User]).toVector
      if(participationInfo.isEmpty) None else {
        val participationInfoMerged: Map[ConversationKey, UserNumbers] = participationInfo.foldLeft(Map[ConversationKey,UserNumbers]())(
          (res: Map[ConversationKey, UserNumbers], pInfo) => {
            val pInfoNumbers : UserNumbers = pInfo.toNumbers()
            val currentValue : Option[UserNumbers] = res.get(pInfo.conversation)
            currentValue match {
              case Some(p) => res + (pInfo.conversation -> p.merge(pInfoNumbers))
              case None => res + (pInfo.conversation -> pInfoNumbers)
            }
          }
        )
        val participations : Vector[User] = participationInfoMerged.foldLeft(Vector[User]())(
          (res: Vector[User], userInfo: (ConversationKey, UserNumbers)) =>
            res.+:(User(name, numberToBool(userInfo._2.participating), numberToBool(userInfo._2.admin), userInfo._1))
        )
        Some(participations)
      }
    }
  }
}

case class UserNumbers(name: String, participating: Int, admin: Int){
  val merge: (UserNumbers) => UserNumbers = (user) => UserNumbers(name, participating + user.participating, admin + user.admin)
}

case class Conversation(participants: Vector[String], admins: Vector[String]){

  // Saves information about Conversation participants
  val save : (ConversationKey) => Boolean = (conversationKey) => {
    val dbKey: String = conversationKey.asKey("")

    // Checking if the conversation exists
    DB.pool.withClient { client =>
      if(!client.lrange(dbKey, 0, 0).isEmpty) false else {
        client.rpush(dbKey, Json.stringify(Json.obj(("sender", "BOT"), ("msg", "Welcome!"))))
        participants.map(
          participant => client.rpush(participant, Json.stringify(Json.toJson(User(participant, true, admins.contains(participant),conversationKey))))
        )
        true
      }
    }
  }
}
object Conversation {
  implicit val conversationWrites: Writes[Conversation] = (
  (JsPath \ "participants").write[Vector[String]] and
  (JsPath \ "admins").write[Vector[String]])(unlift(Conversation.unapply))
}
case class ConversationKey(createdBy: String, conversationName: String){
  implicit private def int2bool(i:Int): Boolean = i != 0

  val asKey : (String) => String = (prefix: String) => prefix + "createdBy:" + createdBy + ",conversationName:" + conversationName
}
object ConversationKey {

  implicit val conversationKeyReads: Reads[ConversationKey] =
    (
      (JsPath \ "createdBy").read[String] and
      (JsPath \ "conversationName").read[String]
    )(ConversationKey.apply _)

    implicit val conversationKeyWrites: Writes[ConversationKey] =
      (
        (JsPath \ "createdBy").write[String] and
        (JsPath \ "conversationName").write[String]
      )(unlift(ConversationKey.unapply))
}
