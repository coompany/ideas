package models.daos.impl

import java.sql.Timestamp

import models.User
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.language.implicitConversions


trait SlickDAO extends HasDatabaseConfigProvider[JdbcProfile] {

    protected val driver: JdbcProfile

    import driver.api._



    // Users table
    case class DBUser(id: Long, firstName: String, lastName: String, email: String)

    protected class UsersTable(tag: Tag) extends Table[DBUser](tag, "user") {
        def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def firstName = column[String]("first_name")
        def lastName = column[String]("last_name")
        def email = column[String]("email")
        override def * = (id, firstName, lastName, email) <> (DBUser.tupled, DBUser.unapply)
    }

    val UsersQuery = TableQuery[UsersTable]



    // Ideas table
    case class DBIdea(id: Long, description: String, creatorId: Long, createdAt: Timestamp)

    protected class IdeasTable(tag: Tag) extends Table[DBIdea](tag, "idea") {
        def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def description = column[String]("description")
        def creatorId = column[Long]("creator_id")
        def createdAt = column[Timestamp]("created_at")
        override def * = (id, description, creatorId, createdAt) <> (DBIdea.tupled, DBIdea.unapply)
    }

    val IdeasQuery = TableQuery[IdeasTable]



    // Implicit conversions
    implicit def dbUserToUser(dbUser: DBUser): User = User(
        id = dbUser.id,
        firstName = dbUser.firstName,
        lastName = dbUser.lastName,
        email = dbUser.email
    )

    implicit def userToDBUser(user: User): DBUser = DBUser(
        id = user.id,
        firstName = user.firstName,
        lastName = user.lastName,
        email = user.email
    )

}
