package models

import com.mohiva.play.silhouette.api.{Identity, LoginInfo}


case class User(id: Long,
                loginInfo: LoginInfo,
                firstName: String,
                lastName: String,
                email: String) extends Identity
