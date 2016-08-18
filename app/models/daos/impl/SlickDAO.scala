package models.daos.impl

import play.api.db.slick.HasDatabaseConfigProvider
import slick.driver.JdbcProfile


trait SlickDAO extends HasDatabaseConfigProvider[JdbcProfile] {

    protected val driver: JdbcProfile

}
