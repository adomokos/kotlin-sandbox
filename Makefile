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
	./gradlew build -x test -x detekt --warning-mode all
.PHONY: build

test: db.rebuild ## Run the tests
	./gradlew test
.PHONY: test

single-test: ## Run a sigle test, pass TEST=something.MySpec to run it
	 @test $(TEST) || (echo "TEST argument is required" ; exit 1)
	./gradlew test --tests "$(TEST)"

.PHONY: tests

run: ## Run app locally
	./gradlew run --args="parallel"
	# ./gradlew run --args="--app=Either --username=adomokos1" - to run the github.explorer app
.PHONY: run

run-all: ## Run  all the examples
	./gradlew run --args="--app=Nullable --username=adomokos"
	./gradlew run --args="--app=Nullable --username=adomoko1"
	./gradlew run --args="--app=Option --username=adomokos"
	./gradlew run --args="--app=Option --username=adomoko1"
	./gradlew run --args="--app=Either --username=adomokos"
	./gradlew run --args="--app=Either --username=adomoko1"
	./gradlew run --args="--app=EitherIO --username=adomokos"
	./gradlew run --args="--app=EitherIO --username=adomoko1"
.PHONY: run-all

run-jar: ## Run the app locally as Jar
	# ./gradlew run --args="parallel"
	./gradlew jar
	java -jar build/libs/kotlin-sandbox-uber.jar
.PHONY: run-jar

run-grpc: ## Runs the grpc subproject
	./gradlew :grpc:run
.PHONY: run-grpc

fix-style: ## Fixed ktlint errors with spotless
	./gradlew spotlessApply
.PHONY: fix-style

complexity: ## Calculates Code Complexity
	./gradlew detekt
.PHONY: complexity

update-check: ## Checks for updates with used libraries
	./gradlew dependencyUpdates -Drevision=release
.PHONY: update-check

help: ## Prints this help command
	@grep -E '^[a-zA-Z0-9\._-]+:.*?## .*$$' $(MAKEFILE_LIST) |\
		sort | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-30s\033[0m %s\n", $$1, $$2}'
.DEFAULT_GOAL := help
