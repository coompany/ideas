package models.daos.impl

import java.sql.Timestamp
import javax.inject.Inject

import models.User
import models.daos.AccessTokenDAO
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future
import scalaoauth2.provider.{AccessToken, AuthInfo}


class SlickAccessTokenDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends AccessTokenDAO with SlickDAO {

    import driver.api._

    override def save(authInfo: AuthInfo[User], accessToken: AccessToken): Future[AccessToken] = {
        val dbAccessToken = DBAccessToken(0, authInfo.user.id, accessToken.token, accessToken.refreshToken,
            accessToken.expiresIn.getOrElse(0), authInfo.clientId.getOrElse(""), authInfo.scope,
            new Timestamp(accessToken.createdAt.getTime))

        val action = for {
            token <- (AccessTokenQuery returning AccessTokenQuery).insertOrUpdate(dbAccessToken)
        } yield token

        db.run(action) map (_ => accessToken)
    }

    override def find(authInfo: AuthInfo[User]): Future[Option[AccessToken]] = {
        val query = for {
            user <- UsersQuery if user.id === authInfo.user.id
            token <- AccessTokenQuery if token.userId === user.id
        } yield token

        db.run(query.result.headOption) map {
            case Some(accessToken) => Some(accessToken)
            case _ => None
        }
    }

    override def find(token: String): Future[Option[AccessToken]] = {
        val query = AccessTokenQuery.filter(_.token === token)

        db.run(query.result.headOption) map {
            case Some(accessToken) => Some(accessToken)
            case _ => None
        }
    }

    override def find(accessToken: AccessToken): Future[Option[AuthInfo[User]]] = {
        val query = for {
            at <- AccessTokenQuery if at.token === accessToken.token
            user <- UsersQuery if user.id === at.userId
        } yield (at, user)

        db.run(query.result.headOption) map {
            case Some((at, user)) => Some(AuthInfo[User](user, Some(at.clientId), at.scope, None))
        }
    }

}
