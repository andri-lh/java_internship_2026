ALTER TABLE events ADD COLUMN image_url varchar(500) NULL;

-- Existing accounts are treated as already verified.
ALTER TABLE users ADD COLUMN email_verified bit(1) NOT NULL DEFAULT b'1';

CREATE TABLE email_verification_tokens (
  id bigint NOT NULL AUTO_INCREMENT,
  created_at datetime(6) NOT NULL,
  expires_at datetime(6) NOT NULL,
  token_hash varchar(64) NOT NULL,
  used_at datetime(6) DEFAULT NULL,
  user_id bigint NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uk_email_verification_token_hash (token_hash),
  KEY fk_email_verification_user (user_id),
  CONSTRAINT fk_email_verification_user FOREIGN KEY (user_id) REFERENCES users (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
