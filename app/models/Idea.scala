package models

import org.joda.time.DateTime


case class Idea(id: Long,
                description: String,
                creator: User,
                createdAt: DateTime)
