
# --- !Ups

CREATE TABLE "user" (
    id         BIGSERIAL,
    email      VARCHAR(255),
    first_name VARCHAR(32),
    last_name  VARCHAR(32),
    PRIMARY KEY (id)
);

# --- !Downs

DROP TABLE "user";
