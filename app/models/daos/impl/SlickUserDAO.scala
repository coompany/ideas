package models.daos.impl

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.providers.CredentialsProvider
import models.User
import models.daos.UserDAO
import play.api.db.slick.DatabaseConfigProvider
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Future


class SlickUserDAO @Inject() (protected val dbConfigProvider: DatabaseConfigProvider)
    extends UserDAO with SlickUserTableDefinition {

    import driver.api._

    override def find(id: Long): Future[Option[User]] =
        headOptionUser(for (user <- UsersQuery if user.id === id) yield user)

    override def find(loginInfo: LoginInfo): Future[Option[User]] =
        headOptionUser(UsersQuery.filter(user => user.email === loginInfo.providerKey))

    private def headOptionUser(query: Query[UsersTable, UsersTable#TableElementType, Seq]): Future[Option[User]] =
        db.run(query.result.headOption) map {
            case Some(user) => Some(
                User(id = user.id,
                    firstName = user.firstName,
                    lastName = user.lastName,
                    email = user.email,
                    loginInfo = LoginInfo(CredentialsProvider.ID, user.email))
                )
            case _ => None
        }

    override def save(user: User): Future[User] = {
        val dbUser = DBUser(
            id = user.id,
            firstName = user.firstName,
            lastName = user.lastName,
            email = user.email)

        val actions = for (
            user <- (UsersQuery returning UsersQuery).insertOrUpdate(dbUser)
        ) yield user

        db.run(actions).map(_ => user)
    }

}


trait SlickUserTableDefinition extends SlickDAO {

    import driver.api._

    case class DBUser(id: Long, firstName: String, lastName: String, email: String)

    protected class UsersTable(tag: Tag) extends Table[DBUser](tag, "user") {
        def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def firstName = column[String]("first_name")
        def lastName = column[String]("last_name")
        def email = column[String]("email")
        override def * = (id, firstName, lastName, email) <> (DBUser.tupled, DBUser.unapply)
    }

    val UsersQuery = TableQuery[UsersTable]

}
