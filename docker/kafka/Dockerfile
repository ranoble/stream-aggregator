FROM wurstmeister/kafka:2.13-2.8.1

RUN mkdir -p /opt/kafka/prometheus
COPY jmx_prometheus_javaagent-0.18.0.jar /opt/kafka/prometheus/jmx_prometheus_javaagent.jar
COPY kafka_metrics.yml /opt/kafka/prometheus/kafka_metrics.yml