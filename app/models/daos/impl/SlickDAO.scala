package models.daos.impl

import java.sql.Timestamp
import java.util.Date

import com.mohiva.play.silhouette.api.util.PasswordInfo
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



    // Votes table
    case class DBVote(userId: Long, ideaId: Long, createdAt: Timestamp)

    protected class VotesTable(tag: Tag) extends Table[DBVote](tag, "vote") {
        def userId = column[Long]("user_id")
        def ideaId = column[Long]("idea_id")
        def createdAt = column[Timestamp]("created_at")
        override def * = (userId, ideaId, createdAt) <> (DBVote.tupled, DBVote.unapply)
        def pk = primaryKey("vote_pkey", (userId, ideaId))
    }

    val VotesQuery = TableQuery[VotesTable]



    // API Access Token table
    case class DBAccessToken(id: Long, userId: Long, token: String, refreshToken: Option[String], expiresIn: Timestamp,
                             clientId: String, scope: Option[String], createdAt: Timestamp)

    protected class AccessTokenTable(tag: Tag) extends Table[DBAccessToken](tag, "api_access_token") {
        def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def userId = column[Long]("user_id")
        def token = column[String]("token")
        def refreshToken = column[Option[String]]("refresh_token")
        def expiresIn = column[Timestamp]("expires_in")
        def clientId = column[String]("client_id")
        def scope = column[Option[String]]("scope")
        def createdAt = column[Timestamp]("created_at")
        override def * = (id, userId, token, refreshToken, expiresIn, clientId, scope, createdAt) <> (DBAccessToken.tupled, DBAccessToken.unapply)
    }

    val AccessTokenQuery = TableQuery[AccessTokenTable]



    // PasswordInfo table
    case class DBPasswordInfo(id: Long, hasher: String, password: String, salt: Option[String], userId: Long)

    protected class PasswordInfoTable(tag: Tag) extends Table[DBPasswordInfo](tag, "password_info") {
        def id = column[Long]("id", O.PrimaryKey, O.AutoInc)
        def hasher = column[String]("hasher")
        def password = column[String]("password")
        def salt = column[Option[String]]("salt")
        def userId = column[Long]("user_id")
        override def * = (id, hasher, password, salt, userId) <> (DBPasswordInfo.tupled, DBPasswordInfo.unapply)
    }

    val PasswordInfoQuery = TableQuery[PasswordInfoTable]



    // Implicit conversions
    implicit def dateTimeToTimestamp(dateTime: DateTime): Timestamp = new Timestamp(dateTime.getMillis)
    implicit def dateToTimestamp(date: Date): Timestamp = new Timestamp(date.getTime)
    implicit def longToTimestamp(long: Long): Timestamp = new Timestamp(long)

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
        lifeSeconds = Some((dbAccessToken.expiresIn.getTime - dbAccessToken.createdAt.getTime) / 1000),
        createdAt = new DateTime(dbAccessToken.createdAt).toDate,
        params = Map()
    )

    def dbIdeaToIdea(dbIdea: DBIdea, user: DBUser): Idea = Idea(
        id = dbIdea.id,
        description = dbIdea.description,
        creator = user,
        createdAt = new DateTime(dbIdea.createdAt)
    )

    implicit def dbPasswordInfoToPasswordInfo(dbPasswordInfo: DBPasswordInfo): PasswordInfo = PasswordInfo(
        hasher = dbPasswordInfo.hasher,
        password = dbPasswordInfo.password,
        salt = dbPasswordInfo.salt
    )



    // common queries
    protected def findUserById(user: User) = UsersQuery.filter(_.id === user.id).result.headOption
    protected def findUserByEmail(email: String) = UsersQuery.filter(_.email === email).result.headOption

    protected def findPasswordInfoByUserId(userId: Long) = PasswordInfoQuery.filter(_.userId === userId).result.headOption

    protected def filterAccessTokensNotExpired(dbAccessToken: AccessTokenTable) =
         dbAccessToken.expiresIn >= new Timestamp(DateTime.now().getMillis)

    protected def findIdeaById(ideaId: Rep[Long]) = IdeasQuery.filter(_.id === ideaId).result.headOption

    protected def countVotesForIdea(ideaId: Rep[Long]) = VotesQuery.filter(_.ideaId === ideaId).countDistinct

}
