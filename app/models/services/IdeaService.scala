package models.services

import models.{Idea, User}

import scala.concurrent.Future


trait IdeaService {

    def getAll: Future[Seq[Idea]]
    def save(idea: Idea): Future[Idea]
    def vote(ideaId: Long, user: User): Future[Option[Idea]]

}
