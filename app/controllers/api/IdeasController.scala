package controllers.api

import javax.inject.{Inject, Singleton}

import auth.OAuthDataHandler
import com.mohiva.play.silhouette.api.util.Clock
import models.Idea
import models.services.IdeaService
import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.mvc.Http.MimeTypes

import scala.concurrent.Future


@Singleton
class IdeasController @Inject() (
    ideaService: IdeaService,
    dataHandler: OAuthDataHandler,
    clock: Clock) extends Controller {

    import scalaoauth2.provider.OAuth2ProviderActionBuilders._

    def index = AuthorizedAction(dataHandler).async { implicit request =>
        ideaService.getAll map { allIdeas =>
            val jsonAll = Json.toJson(allIdeas)
            Ok(jsonAll)
        }
    }

    def create = AuthorizedAction(dataHandler).async { implicit request =>
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
                            creator = request.authInfo.user
                        )
                        ideaService.save(idea) map { idea =>
                            Ok(Json.toJson(idea))
                        }
                    }
                )
            case None => Future.successful(BadRequest(""))
        }
    }

    def vote(ideaId: Long) = AuthorizedAction(dataHandler).async { implicit request =>
        ideaService.vote(ideaId, request.authInfo.user) map {
            case Some(idea) => Ok(Json.toJson(idea))
            case _ => BadRequest("").as(MimeTypes.JSON)
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
