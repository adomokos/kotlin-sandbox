CREATE TABLE people (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  email varchar(255) NOT NULL,
  firstname varchar(255) NOT NULL,
  lastname varchar(255) NOT NULL,
  git_hub_username varchar(255) NOT NULL,
  rating integer(255) DEFAULT 0,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  unique (email, git_hub_username)
);

CREATE TABLE git_hub_metrics (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  person_id INTEGER NOT NULL,
  login varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  public_gists_count INTEGER NOT NULL,
  public_repos_count INTEGER NOT NULL,
  followers_count INTEGER NOT NULL,
  following_count INTEGER NOT NULL,
  most_stargazed_repos TEXT NULL,
  account_created_at DATETIME NOT NULL,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (person_id) REFERENCES people(id)
);
