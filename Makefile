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

test: ## Run a sigle test, pass TEST=something.MySpec to run it
	 @test $(TEST) || (echo "TEST argument is required" ; exit 1)
	./gradlew test --tests $(TEST)
.PHONY: test

tests: ## Run all the tests
	./gradlew cleanTest test
.PHONY: tests

run: ## Run app locally
	./gradlew run
.PHONY: run

help: ## Prints this help command
	@grep -E '^[a-zA-Z0-9_-]+:.*?## .*$$' $(MAKEFILE_LIST) |\
		sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
.DEFAULT_GOAL := help
