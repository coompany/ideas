package controllers

import java.security.SecureRandom
import javax.inject.{Inject, Singleton}

import auth.{DefaultEnv, OAuthDataHandlerImpl}
import com.mohiva.play.silhouette.api.Silhouette
import models.daos.AccessTokenDAO
import play.api.Configuration
import play.api.libs.concurrent.Execution.Implicits._
import play.api.mvc.Controller

import scalaoauth2.provider.{OAuth2Provider, TokenEndpoint}


@Singleton
class OAuth2Controller @Inject() (
    override val tokenEndpoint: TokenEndpoint,
    config: Configuration,
    secureRandom: SecureRandom,
    accessTokenDAO: AccessTokenDAO,
    silhouette: Silhouette[DefaultEnv]) extends Controller with OAuth2Provider {

    def accessToken = silhouette.SecuredAction.async { implicit request =>
        issueAccessToken(new OAuthDataHandlerImpl(config, secureRandom, accessTokenDAO, Some(request.identity)))
    }

}
