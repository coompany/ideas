# --- !Ups
ALTER TABLE api_access_token
  ALTER expires_in SET DATA TYPE TIMESTAMP WITH TIME ZONE
  USING timestamp with time zone 'epoch' + expires_in * interval '1 second';


# --- !Downs
ALTER TABLE api_access_token
  ALTER expires_in TYPE BIGINT;
