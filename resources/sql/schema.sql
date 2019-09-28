CREATE TABLE people (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  email varchar(255) NOT NULL UNIQUE,
  firstname varchar(255),
  lastname varchar(255),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE github_info (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  people_id INTEGER NOT NULL,
  login varchar(255) NOT NULL UNIQUE,
  name varchar(255) NOT NULL,
  account_created_at DATETIME NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (people_id) REFERENCES people(id)
);
