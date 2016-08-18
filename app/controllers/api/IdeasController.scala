package controllers.api

import javax.inject.{Inject, Singleton}

import com.mohiva.play.silhouette.api.Silhouette
import com.mohiva.play.silhouette.api.util.Clock
import models.Idea
import models.Idea._
import models.services.IdeaService
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}
import utils.auth.DefaultEnv

import scala.concurrent.Future


@Singleton
class IdeasController @Inject() (
    ideaService: IdeaService,
    silhouette: Silhouette[DefaultEnv],
    clock: Clock) extends Controller {

    def index = Action.async { implicit request =>
        ideaService.getAll map { allIdeas =>
            val jsonAll = Json.toJson(allIdeas)
            Ok(jsonAll)
        }
    }

    def create = silhouette.SecuredAction.async { implicit request =>
        request.body.asJson match {
            case Some(json) =>
                CreateIdeaRequest.form.bind(json).fold(
                    formWithErrors => {
                        println(formWithErrors)
                        Future.successful(BadRequest(""))
                    },
                    ideaData => {
                        val idea = Idea(
                            id = 0,
                            description = ideaData.description,
                            createdAt = clock.now,
                            creator = request.identity
                        )
                        ideaService.save(idea) map { idea =>
                            Ok(Json.toJson(idea))
                        }
                    }
                )
            case None => Future.successful(BadRequest(""))
        }
    }

}


object CreateIdeaRequest {

    val form = Form(
        mapping(
            "description" -> nonEmptyText
        )(Data.apply)(Data.unapply)
    )

    case class Data(description: String)

}
