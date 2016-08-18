# --- !Ups
CREATE TABLE "api_access_token" (
  id            BIGSERIAL,
  user_id       BIGINT       NOT NULL,
  token         VARCHAR(255) NOT NULL,
  refresh_token VARCHAR(255),
  expires_in    BIGINT       NOT NULL,
  client_id     VARCHAR(255) NOT NULL,
  scope         VARCHAR(255),
  created_at    TIMESTAMP    NOT NULL,
  PRIMARY KEY (id)
);


# --- !Downs
DROP TABLE "api_access_token";
