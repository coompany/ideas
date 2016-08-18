package models.daos.impl

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import models.daos.UserDAO
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future


class SlickUserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider) extends UserDAO with SlickDAO {

    import driver.api._

    override def find(id: Long): Future[Option[User]] =
        headOptionUser(for (user <- UsersQuery if user.id === id) yield user)

    override def find(loginInfo: LoginInfo): Future[Option[User]] =
        headOptionUser(UsersQuery.filter(user => user.email === loginInfo.providerKey))

    private def headOptionUser(query: Query[UsersTable, UsersTable#TableElementType, Seq]): Future[Option[User]] =
        db.run(query.result.headOption) map {
            case Some(user) => Some(user)
            case _ => None
        }

    override def save(user: User): Future[User] = {
        val actions = for (
            user <- (UsersQuery returning UsersQuery).insertOrUpdate(user)
        ) yield user

        db.run(actions).map(_ => user)
    }

}
