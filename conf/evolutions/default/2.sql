# --- !Ups

CREATE TABLE "idea" (
  id          BIGSERIAL,
  description TEXT      NOT NULL,
  creator_id  BIGINT    NOT NULL,
  created_at  TIMESTAMP NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE "vote" (
  user_id    BIGINT    NOT NULL,
  idea_id    BIGINT    NOT NULL,
  created_at TIMESTAMP NOT NULL,
  PRIMARY KEY (user_id, idea_id)
);


# --- !Downs
DROP TABLE "idea";
DROP TABLE "vote";
