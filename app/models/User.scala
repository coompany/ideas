package models

import com.mohiva.play.silhouette.api.Identity


case class User(id: Long,
                firstName: String,
                lastName: String,
                email: String) extends Identity
