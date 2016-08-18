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

        val action = for (
            idea <- (IdeasQuery returning IdeasQuery).insertOrUpdate(dbIdea)
        ) yield idea

        db.run(action).map(_ => idea)
    }

}
