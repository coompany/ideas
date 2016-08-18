package models.daos.impl

import java.sql.Timestamp
import javax.inject.Inject

import models.Idea
import models.daos.IdeaDAO
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future


class SlickIdeaDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends IdeaDAO with SlickDAO {

    import driver.api._

    override def all: Future[Seq[Idea]] = {
        val query = for {
            idea <- IdeasQuery.sortBy(_.createdAt)
            creator <- UsersQuery if creator.id === idea.creatorId
        } yield (idea, creator)

        db.run(query.result) map { rows =>
            rows map {
                case (idea, creator) => Idea(idea.id, idea.description, creator, new DateTime(idea.createdAt))
            }
        }
    }

    override def save(idea: Idea): Future[Idea] = {
        val dbIdea = DBIdea(
            id = idea.id,
            description = idea.description,
            creatorId = idea.creator.id,
            createdAt = new Timestamp(idea.createdAt.getMillis)
        )

        val selectCreator = UsersQuery.filter(_.id === idea.creator.id)

        val insIdea = (IdeasQuery returning IdeasQuery.map(_.id) into ((idea, id) => idea.copy(id = id))).insertOrUpdate(dbIdea)

        val actions = for {
            user <- selectCreator.result.head
            idea <- insIdea
        } yield (user, idea)

        db.run(actions) map {
            case (user, newIdea) => dbIdeaToIdea(newIdea.get, user)
        }
    }

}
