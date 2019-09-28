mkfile_path := $(abspath $(lastword $(MAKEFILE_LIST)))
current_dir := $(notdir $(patsubst %/,%,$(dir $(mkfile_path))))

db.rebuild: ## Rebuilds the DBs
	@echo 'Rebuilding the DB...'
	@sh resources/rebuild-db.sh run
	@echo 'Done.'
.PHONY: rebuild-dbs

db.console: ## Open the db-console
	sqlite3 db/explorer-db.sqlt
.PHONY: db-console

build: ## Build with Gradle
	./gradlew build
.PHONY: build

test: ## Run the tests
	./gradlew clean test
.PHONY: test

run: build ## Run app locally
	./gradelew run
.PHONY: run

help: ## Display this message
	@echo "$$(grep -hE '^\S+:.*##' $(MAKEFILE_LIST) | sed -e 's/:.*##\s*/:/' -e 's/^\(.\+\):\(.*\)/\\x1b[36m\1\\x1b[m:\2/' | column -c2 -t -s :)"
.PHONY: help
.DEFAULT_GOAL := help
