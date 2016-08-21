package models.daos.impl

import javax.inject.Inject

import models.daos.IdeaDAO
import models.{Idea, User}
import org.joda.time.DateTime
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future


class SlickIdeaDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends IdeaDAO with SlickDAO {

    import driver.api._

    override def all: Future[Seq[Idea]] = {
        val query = IdeasQuery sortBy (_.createdAt) join UsersQuery on (_.creatorId === _.id) map {
            case (idea, creator) => (idea, creator, countVotesForIdea(idea.id))
        }

        db run query.result map { rows =>
            rows map { case (idea, user, votes) => dbIdeaToIdea(idea, user) copy (votes = votes) }
        }
    }

    override def save(idea: Idea): Future[Idea] = {
        val dbIdea = DBIdea(
            id = idea.id,
            description = idea.description,
            creatorId = idea.creator.id,
            createdAt = idea.createdAt
        )

        val selectCreator = UsersQuery filter (_.id === idea.creator.id)

        val insIdea = IdeasQuery returning IdeasQuery.map(_.id) into ((idea, id) => idea copy (id = id)) insertOrUpdate dbIdea

        val actions = for {
            user <- selectCreator.result.head
            idea <- insIdea
        } yield (user, idea)

        db run actions map {
            case (user, newIdea) => dbIdeaToIdea(newIdea.get, user)
        }
    }

    override def vote(ideaId: Long, user: User): Future[Option[Idea]] = {
        val action = findIdeaById(ideaId) flatMap {
            case Some(idea) => for {
                _ <- VotesQuery += DBVote(user.id, ideaId, DateTime.now())
                votes <- countVotesForIdea(ideaId).result
            } yield Some((idea, votes))
            case _ => DBIO successful None
        }

        db run action map {
            case Some((idea, votes)) => Some(dbIdeaToIdea(idea, user) copy (votes = votes))
            case _ => None
        }
    }

}
