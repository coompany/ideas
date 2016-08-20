import org.joda.time.DateTime
import play.api.libs.functional.syntax._
import play.api.libs.json._

package object models {

    implicit val userWrites = new Writes[User] {
        override def writes(o: User): JsValue = Json.obj(
            "id" -> o.id,
            "firstName" -> o.firstName,
            "lastName" -> o.lastName,
            "email" -> o.email
        )
    }

    implicit val userReads: Reads[User] = (
        (JsPath \ "id").read[Long] and
            (JsPath \ "firstName").read[String] and
            (JsPath \ "lastName").read[String] and
            (JsPath \ "email").read[String]
        )(User.apply _)


    implicit val ideaWrites = new Writes[Idea] {
        override def writes(o: Idea): JsValue = Json.obj(
            "id" -> o.id,
            "description" -> o.description,
            "creator" -> o.creator,
            "createdAt" -> o.createdAt,
            "votes" -> o.votes
        )
    }

    implicit val ideaReads: Reads[Idea] = (
        (JsPath \ "id").read[Long] and
            (JsPath \ "description").read[String] and
            (JsPath \ "creator").read[User] and
            (JsPath \ "createdAt").read[DateTime] and
            (JsPath \ "votes").read[Int]
        )(Idea.apply _)

}
