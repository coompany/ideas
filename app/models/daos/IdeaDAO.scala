package models.daos

import models.Idea

import scala.concurrent.Future


trait IdeaDAO {

    def all: Future[Seq[Idea]]
    def save(idea: Idea): Future[Idea]

}
