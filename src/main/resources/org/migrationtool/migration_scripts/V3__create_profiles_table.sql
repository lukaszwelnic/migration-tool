DROP TABLE IF EXISTS profiles;

CREATE TABLE profiles (
  id SERIAL PRIMARY KEY,
  user_id INT UNIQUE NOT NULL,
  address VARCHAR(255),
  city VARCHAR(255),
  postal_code VARCHAR(10),
  date_of_birth VARCHAR(20) NOT NULL,
  id_card_number VARCHAR(20) UNIQUE NOT NULL,
  FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);