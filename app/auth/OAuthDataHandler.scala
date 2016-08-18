package auth

import java.security.SecureRandom
import java.util.Base64
import javax.inject.Inject

import com.google.inject.assistedinject.Assisted
import models.User
import models.daos.AccessTokenDAO
import org.joda.time.DateTime
import play.api.Configuration

import scala.concurrent.Future
import scalaoauth2.provider.{AccessToken, AuthInfo, AuthorizationRequest, DataHandler}


sealed trait OAuthDataHandler extends DataHandler[User]


class OAuthDataHandlerImpl @Inject() (
    config: Configuration,
    secureRandom: SecureRandom,
    accessTokenDAO: AccessTokenDAO,
    user: Option[User]) extends OAuthDataHandler {

    val accessTokenExpire = Some(config.getMilliseconds("oauth2.tokenExpire").getOrElse(60 * 60L * 1000) / 1000)

    override def findAuthInfoByAccessToken(accessToken: AccessToken): Future[Option[AuthInfo[User]]] = accessTokenDAO.find(accessToken)

    override def findAccessToken(token: String): Future[Option[AccessToken]] = accessTokenDAO.find(token)

    override def validateClient(request: AuthorizationRequest): Future[Boolean] = Future.successful(true)

    override def findUser(request: AuthorizationRequest): Future[Option[User]] = Future.successful(user)

    override def createAccessToken(authInfo: AuthInfo[User]): Future[AccessToken] = {
        val refreshToken = Some(generateToken())
        val accessToken = generateToken()
        val now = DateTime.now().toDate

        val tokenObject = AccessToken(accessToken, refreshToken, authInfo.scope, accessTokenExpire, now)
        accessTokenDAO.save(authInfo, tokenObject)

        Future.successful(tokenObject)
    }

    override def getStoredAccessToken(authInfo: AuthInfo[User]): Future[Option[AccessToken]] = accessTokenDAO.find(authInfo)

    override def refreshAccessToken(authInfo: AuthInfo[User], refreshToken: String): Future[AccessToken] = ???

    override def findAuthInfoByCode(code: String): Future[Option[AuthInfo[User]]] = ???

    override def deleteAuthCode(code: String): Future[Unit] = Future.failed(new NotImplementedError())

    override def findAuthInfoByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]] = ???


    private def generateToken(length: Int = 256): String = BigInt(length, secureRandom).toString(32)

}
