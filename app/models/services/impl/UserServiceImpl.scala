package models.services.impl

import javax.inject.Inject

import com.mohiva.play.silhouette.api.LoginInfo
import models.User
import models.daos.UserDAO
import models.services.UserService

import scala.concurrent.Future


class UserServiceImpl @Inject() (userDAO: UserDAO) extends UserService {

    override def retrieve(loginInfo: LoginInfo): Future[Option[User]] = userDAO.find(loginInfo)

    override def save(user: User): Future[User] = userDAO.save(user)

}
