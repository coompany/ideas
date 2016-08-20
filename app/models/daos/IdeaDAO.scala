package models.daos

import models.{Idea, User}

import scala.concurrent.Future


trait IdeaDAO {

    def all: Future[Seq[Idea]]
    def save(idea: Idea): Future[Idea]
    def vote(ideaId: Long, user: User): Future[Option[Idea]]

}
