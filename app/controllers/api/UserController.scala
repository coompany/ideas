package controllers.api

import javax.inject.{Inject, Singleton}

import auth.OAuthDataHandler
import models._
import play.api.libs.json.Json
import play.api.mvc.Controller


@Singleton
class UserController @Inject()(
    dataHandler: OAuthDataHandler) extends Controller {

    import scalaoauth2.provider.OAuth2ProviderActionBuilders._

    def me = AuthorizedAction(dataHandler) { implicit request =>
        Ok(Json.toJson(request.authInfo.user))
    }

}
