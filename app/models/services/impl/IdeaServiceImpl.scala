package models.services.impl

import javax.inject.Inject

import models.Idea
import models.daos.IdeaDAO
import models.services.IdeaService

import scala.concurrent.Future


class IdeaServiceImpl @Inject() (ideaDAO: IdeaDAO) extends IdeaService {

    override def getAll: Future[Seq[Idea]] = ideaDAO.all

    override def save(idea: Idea): Future[Idea] = ideaDAO.save(idea)

}
