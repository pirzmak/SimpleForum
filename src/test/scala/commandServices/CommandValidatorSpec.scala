package commandServices

import confguration.ValidationConfig
import model.{Post, PostId, PostSecret, PostSecretGenerator, Topic, TopicId, User}
import org.scalatest.{AsyncFlatSpec, BeforeAndAfter, MustMatchers}
import repositories.slick.mocks.{PostsRepositoryMock, TopicsRepositoryMock}

class CommandValidatorSpec
  extends AsyncFlatSpec
    with MustMatchers
    with BeforeAndAfter {

  val topicsRepository = new TopicsRepositoryMock()
  val postsRepository = new PostsRepositoryMock(topicsRepository)
  topicsRepository.init(Seq(Topic("test", "test", User("asd", "asd@asd"))))
  postsRepository.init(Seq(Post(TopicId(1), "test", User("asd", "asd@asd"))))
  val config = ValidationConfig("""(\w+)@([\w\.]+)""", 2, 10, 2, 10)
  val commandValidator = new CommandValidator(topicsRepository, postsRepository, config)

  "Command validator when validate too short post message" should "return PostValidationTooShortFailure" in {
    commandValidator.validatePostMessage("a") mustBe Some(CommandFailure.PostValidationTooShortFailure)
  }

  "Command validator when validate too long post message" should "return PostValidationTooLongFailure" in {
    commandValidator.validatePostMessage("a" * 11) mustBe Some(CommandFailure.PostValidationTooLongFailure)
  }

  "Command validator when validate correct post message" should "return None" in {
    commandValidator.validatePostMessage("aaa") mustBe None
  }

  "Command validator when validate too short topic title" should "return PostValidationTooLongFailure" in {
    commandValidator.validateTopicTitle("a") mustBe Some(CommandFailure.TopicTitleValidationTooShortFailure)
  }

  "Command validator when validate too long topic title" should "return PostValidationTooLongFailure" in {
    commandValidator.validateTopicTitle("a" * 11) mustBe Some(CommandFailure.TopicTitleValidationTooLongFailure)
  }

  "Command validator when validate correct topic title" should "return None" in {
    commandValidator.validateTopicTitle("aaa") mustBe None
  }

  "Command validator when validate incorrect email" should "return EmailIncorrectValidationFailure" in {
    commandValidator.validateEmail("asdf") mustBe Some(CommandFailure.EmailIncorrectValidationFailure)
  }

  "Command validator when validate correct email" should "return None" in {
    commandValidator.validateEmail("aaa@aaa.pl") mustBe None
  }

  "Command validator when validate incorrect postSecret" should "return IncorrectPostSecretFailure" in {
    commandValidator.validateSecretPost(PostSecret("asd")) mustBe Some(CommandFailure.IncorrectPostSecretFailure)
  }

  "Command validator when validate correct postSecret" should "return None" in {
    commandValidator.validateSecretPost(PostSecretGenerator.getPostSecret(PostId(0))) mustBe None
  }

  "Command validator when validate not existing post" should "return PostIdNotFoundFailure" in {
    commandValidator.validateIfPostExists(PostId(0)) map {
      result => result mustBe Some(CommandFailure.PostIdNotFoundFailure)
    }
  }

  "Command validator when validate existing post" should "return None" in {
    commandValidator.validateIfPostExists(PostId(1)) map {
      result => result mustBe None
    }
  }

  "Command validator when validate not existing topic" should "return TopicIdNotFoundFailure" in {
    commandValidator.validateIfTopicExists(TopicId(0)) map {
      result => result mustBe Some(CommandFailure.TopicIdNotFoundFailure)
    }
  }

  "Command validator when validate existing topic" should "return None" in {
    commandValidator.validateIfTopicExists(TopicId(1)) map {
      result => result mustBe None
    }
  }
}
