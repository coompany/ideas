package models.daos.impl

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.util.PasswordInfo
import com.mohiva.play.silhouette.persistence.daos.DelegableAuthInfoDAO
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future


class SlickPasswordInfoDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
    extends DelegableAuthInfoDAO[PasswordInfo] with SlickDAO {

    import driver.api._

    override def find(loginInfo: LoginInfo): Future[Option[PasswordInfo]] = {
        val query = for {
            user <- findUserByEmail(loginInfo.providerKey)
            pi <- findPasswordInfoByUserId(user.get.id) if user.isDefined
        } yield pi

        db.run(query) map {
            case Some(pi) => Some(pi)
            case _ => None
        }
    }

    override def add(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
        val dbPasswordInfo = DBPasswordInfo(0, authInfo.hasher, authInfo.password, authInfo.salt, 0)
        val action = for {
            user <- findUserByEmail(loginInfo.providerKey)
            _ <- PasswordInfoQuery += dbPasswordInfo.copy(userId = user.get.id) if user.isDefined
        } yield ()

        db.run(action) map { _ => authInfo }
    }

    override def update(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
        val action = for {
            user <- findUserByEmail(loginInfo.providerKey)
            pi <- findPasswordInfoByUserId(user.get.id) if user.isDefined
            upi <- (PasswordInfoQuery returning PasswordInfoQuery).insertOrUpdate(pi.get.copy(
                hasher = authInfo.hasher,
                password = authInfo.password,
                salt = authInfo.salt
            ))
        } yield upi

        db run action map (pi => pi.get)
    }

    override def save(loginInfo: LoginInfo, authInfo: PasswordInfo): Future[PasswordInfo] = {
        val action = for {
            user <- findUserByEmail(loginInfo.providerKey)
            pi <- (PasswordInfoQuery returning PasswordInfoQuery).insertOrUpdate(DBPasswordInfo(
                id = 0,
                hasher = authInfo.hasher,
                password = authInfo.password,
                salt = authInfo.salt,
                userId = user.get.id
            ))
        } yield pi

        db run action map (pi => pi.get)
    }

    override def remove(loginInfo: LoginInfo): Future[Unit] = {
        val action = for {
            user <- findUserByEmail(loginInfo.providerKey)
            _ <- PasswordInfoQuery.filter(_.userId === user.get.id).delete
        } yield ()

        db run action
    }

}
