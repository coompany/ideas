package controllers

import java.security.SecureRandom
import javax.inject.{Inject, Singleton}

import auth.{DefaultEnv, OAuthDataHandlerImpl}
import com.mohiva.play.silhouette.api.Silhouette
import models.daos.AccessTokenDAO
import play.api.Configuration
import play.api.libs.json.Json
import play.api.mvc.{Controller, Request, Result}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.ExecutionContext.Implicits._
import scalaoauth2.provider.{AuthorizationHandler, OAuth2Provider, OAuthGrantType, TokenEndpoint}


@Singleton
class OAuth2Controller @Inject() (
    override val tokenEndpoint: TokenEndpoint,
    config: Configuration,
    secureRandom: SecureRandom,
    accessTokenDAO: AccessTokenDAO,
    silhouette: Silhouette[DefaultEnv]) extends Controller with OAuth2Provider {

    def accessToken = silhouette.UserAwareAction.async { implicit request =>
        request.identity match {
            case Some(user) =>
                issueAccessToken(new OAuthDataHandlerImpl(config, secureRandom, accessTokenDAO, Some(user)))
            case _ => Future.successful(Redirect(routes.AuthController.getSignIn()))
        }
    }

    override def issueAccessToken[A, U](handler: AuthorizationHandler[U])(implicit request: Request[A], ctx: ExecutionContext): Future[Result] = {
        tokenEndpoint.handleRequest(request, handler).map {
            case Left(e) => new Status(e.statusCode)(responseOAuthErrorJson(e)).withHeaders(responseOAuthErrorHeader(e))
            case Right(r) =>
                r.authInfo.redirectUri match {
                    case Some(uri) if request.grantType == OAuthGrantType.IMPLICIT =>
                        Redirect(uri + "#" + r.accessToken)
                    case _ =>
                        Ok(Json.toJson(responseAccessToken(r))).withHeaders("Cache-Control" -> "no-store", "Pragma" -> "no-cache")
                }
        }
    }

}
