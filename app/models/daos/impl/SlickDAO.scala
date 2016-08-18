package models.daos.impl

import java.sql.Timestamp

import models.{Idea, User}
import org.joda.time.DateTime
import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile

import scala.language.implicitConversions
import scalaoauth2.provider.AccessToken


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



    // API Access Token table
    case class DBAccessToken(id: Long, userId: Long, token: String, refreshToken: Option[String], expiresIn: Long,
                             clientId: String, scope: Option[String], createdAt: Timestamp)

    protected class AccessTokenTable(tag: Tag) extends Table[DBAccessToken](tag, "api_access_token") {
        def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def userId = column[Long]("user_id")
        def token = column[String]("token")
        def refreshToken = column[Option[String]]("refresh_token")
        def expiresIn = column[Long]("expires_in")
        def clientId = column[String]("client_id")
        def scope = column[Option[String]]("scope")
        def createdAt = column[Timestamp]("created_at")
        override def * = (id, userId, token, refreshToken, expiresIn, clientId, scope, createdAt) <> (DBAccessToken.tupled, DBAccessToken.unapply)
    }

    val AccessTokenQuery = TableQuery[AccessTokenTable]



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

    implicit def dbAccessTokenToAccessToken(dbAccessToken: DBAccessToken): AccessToken = AccessToken(
        token = dbAccessToken.token,
        refreshToken = dbAccessToken.refreshToken,
        scope = dbAccessToken.scope,
        lifeSeconds = Some(dbAccessToken.expiresIn),
        createdAt = new DateTime(dbAccessToken.createdAt).toDate,
        params = Map()
    )

    def dbIdeaToIdea(dbIdea: DBIdea, user: DBUser): Idea = Idea(
        id = dbIdea.id,
        description = dbIdea.description,
        creator = user,
        createdAt = new DateTime(dbIdea.createdAt)
    )

}
