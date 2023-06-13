#set up a makefile to a: update the build.gradle build file, then build for a specific target.
#cp build/libs/CheckoutTest-0.1-SNAPSHOT-all.jar jobs/aggregator.jar
.PHONY: build docker-images all
build: clean jar docker-images
all: build run

run:
	docker-compose up

test:
	./gradlew test

jar:
	./gradlew shadowJar
	mv build/libs/CheckoutTest-0.1-SNAPSHOT-all.jar docker/taskrunner/aggregator.jar

clean:
	./gradlew clean
	rm docker/taskrunner/aggregator.jar

docker-images:
	docker build -f docker/flink/Dockerfile -t docker.io/library/flink-test:latest docker/flink/.
	docker build -f docker/kafka/Dockerfile -t docker.io/library/kafka-with-metrics:latest docker/kafka/.

sql:
	docker-compose exec jobmanager /opt/flink/bin/sql-client.sh -i /opt/flink/catalog.sql