FROM apache/flink:1.17.1-scala_2.12-java11

# Download connector libraries
RUN wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-sql-connector-kafka/1.17.1/flink-sql-connector-kafka-1.17.1.jar; \
    wget -P /opt/flink/lib/ https://repo.maven.apache.org/maven2/org/apache/flink/flink-json/1.17.1/flink-json-1.17.1.jar;

RUN mkdir -p /opt/tasks && \
    mkdir -p /tmp/data && chown -R flink /tmp/data

RUN echo "execution.checkpointing.interval: 10s" >> /opt/flink/conf/flink-conf.yaml; \
    echo "pipeline.object-reuse: true" >> /opt/flink/conf/flink-conf.yaml; \
    echo "pipeline.time-characteristic: EventTime" >> /opt/flink/conf/flink-conf.yaml; \
    echo "taskmanager.numberOfTaskSlots: 8" >> /opt/flink/conf/flink-conf.yaml; \
    echo "metrics.reporter.jmx.factory.class: org.apache.flink.metrics.jmx.JMXReporterFactory" >> /opt/flink/conf/flink-conf.yaml; \
    echo "metrics.reporter.jmx.port: 8789" >> /opt/flink/conf/flink-conf.yaml; \
    echo "metrics.reporters: prom,jmx" >> /opt/flink/conf/flink-conf.yaml; \
    echo "metrics.reporter.prom.factory.class: org.apache.flink.metrics.prometheus.PrometheusReporterFactory" >> /opt/flink/conf/flink-conf.yaml; \
    echo "taskmanager.memory.jvm-metaspace.size: 256m" >> /opt/flink/conf/flink-conf.yaml;