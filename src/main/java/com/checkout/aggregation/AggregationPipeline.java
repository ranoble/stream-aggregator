package com.checkout.aggregation;

import org.apache.commons.text.StringSubstitutor;
import org.apache.flink.table.api.*;

import java.util.Map;
import java.util.Optional;

import static com.checkout.tables.Catalog.*;
import static org.apache.flink.table.api.Expressions.*;

public class AggregationPipeline {

    private final String bootstrap;
    private final String consumerGroup;
    private TableEnvironment env;
    private StatementSet statements;

    public AggregationPipeline(String bootstrap, String consumerGroup) {
        this.bootstrap = bootstrap;
        this.consumerGroup = consumerGroup;
    }

    public static void main(String[] args) throws Exception {
        String consumerGroup = Optional.ofNullable(System.getenv("CONSUMER_GROUP")).orElse("streaming-consumer");
        String bootstrap = Optional.ofNullable(System.getenv("KAFKA_BOOTSTRAP")).orElse("kafka:9092");

        AggregationPipeline pipeline = new AggregationPipeline(bootstrap, consumerGroup);
        pipeline.buildEnvironment()
                .registerSources()
                .registerSinks()
                .defineProcessors()
                .execute();
    }

    /**
     * Set up the Flink environment.
     * @return
     */
    private AggregationPipeline buildEnvironment() {
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .inStreamingMode()
                .build();
        this.env = TableEnvironment.create(settings);
        return this;
    }

    /**
     * The pipelines to run, in this case, aggregation and passthrough.
     * Add any new steps to the pipeline here.
     * @return the executable statement set.
     */
    private AggregationPipeline defineProcessors() {
        StatementSet statements = env.createStatementSet();
        statements.addInsert("PageViewsFS", passThrough(env.from("PageViews")));
        statements.addInsert("PageViewsAggregate", aggregateViews(env.from("PageViews")));
        this.statements = statements;
        return this;
    }

    private TableResult execute() {
        return statements.execute();
    }

    /**
     * Register the input sources.
     * @return
     */
    private AggregationPipeline registerSources() {
        env.executeSql(StringSubstitutor.replace(
                PAGE_VIEWS_STREAM, Map.of("CONSUMER_GROUP", consumerGroup, "KAFKA_BOOTSTRAP", bootstrap),
                "${", "}"));
        return this;
    }

    /**
     * Register the output Sinks
     * @return
     */
    private AggregationPipeline registerSinks() {
        env.executeSql(PAGE_VIEWS_FILESYSTEM);
        env.executeSql(PAGE_VIEW_AGGREGATE);
        return this;
    }

    /**
     * Enhance the input source by adding dat and hour, useful for partitioninng in a filesystem.
     * @param source
     * @return output Table.
     */
    public Table passThrough(Table source) {
        return source.select($("user_id"),
                $("postcode"),
                $("timestamp"),
                $("webpage"),
                dateFormat($("timestamp"), "yyyy-MM-dd").as("day"),
                dateFormat($("timestamp"), "HH").as("hour"));
    }

    /**
     * Aggregate the data from the source input into windows by minute and by postcode.
     * @param source
     * @return the output, ready for materialisation
     */
    public Table aggregateViews(Table source) {
        return source
                .window(Tumble.over(lit(1).minute()).on($("timestamp")).as("window"))
                .groupBy($("postcode"), $("window"))
                .select($("postcode"),
                        $("window").end().as("window"),
                        $("webpage").count().as("views"),
                        dateFormat($("window").end(), "yyyy-MM-dd").as("day"),
                        dateFormat($("window").end(), "HH").as("hour"));
    }
}