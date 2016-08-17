package models.services

import java.util.concurrent.atomic.AtomicInteger

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.api.services.IdentityService
import models.User

import scala.collection.mutable
import scala.concurrent.Future


trait UserService extends IdentityService[User] {

    def save(user: User): Future[User]

}


class UserServiceImpl extends UserService {

    import UserServiceImpl._

    override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = Future.successful(users.get(loginInfo))

    override def save(user: User): Future[User] = {
        val withId = user.copy(id = atomicInteger.getAndIncrement())
        users += user.loginInfo -> withId
        Future.successful(withId)
    }

}


object UserServiceImpl {

    val atomicInteger = new AtomicInteger()
    atomicInteger.set(1)
    val users = mutable.Map.empty[LoginInfo, User]

}
