#!/bin/bash

/opt/flink/bin/flink run -Dexecution.target=remote --jobmanager jobmanager:8081 -Dpipeline.name=datagenerator -c com.checkout.datagen.DataGen --parallelism=1 /opt/tasks/aggregator.jar
/opt/flink/bin/flink run -Dexecution.target=remote --jobmanager jobmanager:8081 -Dpipeline.name=aggregator -Dtable.exec.source.idle-timeout=1000ms --parallelism=1 /opt/tasks/aggregator.jar