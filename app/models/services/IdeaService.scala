package models.services

import models.Idea

import scala.concurrent.Future


trait IdeaService {

    def getAll: Future[Seq[Idea]]
    def save(idea: Idea): Future[Idea]

}
