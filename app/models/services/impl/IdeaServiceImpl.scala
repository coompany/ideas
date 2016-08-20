package models.services.impl

import javax.inject.Inject

import models.daos.IdeaDAO
import models.services.IdeaService
import models.{Idea, User}

import scala.concurrent.Future


class IdeaServiceImpl @Inject() (ideaDAO: IdeaDAO) extends IdeaService {

    override def getAll: Future[Seq[Idea]] = ideaDAO.all

    override def save(idea: Idea): Future[Idea] = ideaDAO.save(idea)

    override def vote(ideaId: Long, user: User): Future[Option[Idea]] = ideaDAO.vote(ideaId, user)

}
