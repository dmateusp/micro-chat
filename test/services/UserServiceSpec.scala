import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play._
import play.api.test.Helpers._
import org.mockito.Mockito._
import org.mockito.Matchers._
import models._
import services._

// https://www.playframework.com/documentation/latest/ScalaTestingWithScalaTest
/*
Some(
  Vector(
    User("Smith", true, true, ConversationKey("Smith", "Smith and O'Connell chatroom")),
    User("Smith", false, true, ConversationKey("Smith", "Smith and O'Connell chatroom"))
  )
)*/

class UserServiceSpec extends PlaySpec with MockitoSugar {

  "UserService#getParticipations" should {
    "return that a user is not participating to a conversation" in {

      // init
      val userRepository = mock[UserRepository]

      // setup
      when(userRepository.getConversations(anyString())).thenReturn(
        Some(
          Vector(
            User("Smith", true, true, ConversationKey("Smith", "Smith and O'Connell chatroom")),
            User("Smith", false, true, ConversationKey("Smith", "Smith and O'Connell chatroom"))
          )
        )
      )
      val userService : UserService = new UserService(userRepository)
      // run
      val actual = userService.getParticipations("Smith")

      // verify
      actual mustBe Some(Vector(User("Smith", false, true, ConversationKey("Smith", "Smith and O'Connell chatroom"))))
    }
  }
}
