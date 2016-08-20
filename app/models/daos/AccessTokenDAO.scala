package models.daos

import models.User

import scala.concurrent.Future
import scalaoauth2.provider.{AccessToken, AuthInfo}


trait AccessTokenDAO {

    def save(authInfo: AuthInfo[User], accessToken: AccessToken): Future[AccessToken]
    def find(authInfo: AuthInfo[User]): Future[Option[AccessToken]]
    def find(token: String): Future[Option[AccessToken]]
    def find(accessToken: AccessToken): Future[Option[AuthInfo[User]]]
    def findByRefreshToken(refreshToken: String): Future[Option[AuthInfo[User]]]
    def delete(authInfo: AuthInfo[User]): Future[Unit]

}
