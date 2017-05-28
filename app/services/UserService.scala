package services

import models._
import db._
import play.api.libs.json._
/*
Play does not require models to use a particular database data access layer.
However,if the application uses Anorm or Slick, then frequently the Model will
have a reference to database access internally.

For unit testing, this approach can make mocking out a method tricky.

A common approach is to keep the models isolated from the database
and as much logic as possible, and abstract database access behind a repository layer.
*/

trait UserRepository {
  def getConversations(user: String) : Option[Vector[User]]
}


class RedisUserRepository extends UserRepository {
  def getConversations(user: String) : Option[Vector[User]] = {
    DB.pool.withClient { client =>
      val participationsRaw: Vector[User] = client.lrange(user, 0, -1).map((u: String) => Json.parse(u).as[User]).toVector
      if(participationsRaw.isEmpty) None else Some(participationsRaw)
    }
  }
}

class UserService(userRepository: UserRepository){
  private val boolToNumber : (Boolean) => Int = (bool) => if(bool) 1 else -1
  private val countToBool : (Int) => Boolean = (int) => int > 0

  def getParticipations(user: String) : Option[Vector[User]] = {
    userRepository.getConversations(user) match {
      case Some(participationsRaw) => {
        val participationInfoMerged: Map[ConversationKey, UserNumbers] = participationsRaw.foldLeft(Map[ConversationKey,UserNumbers]())(
          (res: Map[ConversationKey, UserNumbers], pInfo) => {
            val pInfoNumbers : UserNumbers = pInfo.toNumbers()
            val currentValue : Option[UserNumbers] = res.get(pInfo.conversation)
            currentValue match {
              case Some(p) => res - pInfo.conversation + (pInfo.conversation -> p.merge(pInfoNumbers))
              case None => res - pInfo.conversation + (pInfo.conversation -> pInfoNumbers)
            }
          }
        )
        val participations : Vector[User] = participationInfoMerged.foldLeft(Vector[User]())(
          (res: Vector[User], userInfo: (ConversationKey, UserNumbers)) =>
            res.+:(User(user, countToBool(userInfo._2.participating), countToBool(userInfo._2.admin), userInfo._1))
        )
        Some(participations)
      }
      case None => None
    }
  }
}
