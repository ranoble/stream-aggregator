package com.checkout.aggregation;

import com.checkout.aggregation.AggregationPipeline;
import lombok.SneakyThrows;
import org.apache.flink.table.api.*;
import org.apache.flink.types.Row;
import org.apache.flink.util.CloseableIterator;
import org.junit.jupiter.api.Test;
import org.testcontainers.shaded.org.hamcrest.MatcherAssert;
import org.testcontainers.shaded.org.hamcrest.Matchers;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class AggregationPipelineTest {

    private static final LocalDateTime DATE_TIME = LocalDateTime.of(2020, 1, 1, 0, 0);

    /**
     * Test that we correctly create the partition fields with the writethrough.
     */
    @Test
    @SneakyThrows
    public void testPassthrough() {
        EnvironmentSettings settings = EnvironmentSettings.newInstance().inBatchMode().build();
        TableEnvironment tEnv = TableEnvironment.create(settings);

        Table views =
                tEnv.fromValues(
                        DataTypes.ROW(
                                DataTypes.FIELD("user_id", DataTypes.INT().notNull()),
                                DataTypes.FIELD("postcode", DataTypes.STRING().notNull()),
                                DataTypes.FIELD("timestamp", DataTypes.TIMESTAMP(3)),
                                DataTypes.FIELD("webpage", DataTypes.STRING().notNull())),
                        Row.of(1, "SW1A 1AA", DATE_TIME.plusSeconds(5), "www.website.com/faq.html"),
                        Row.of(2, "SW1A 1AA", DATE_TIME.plusSeconds(47), "www.website.com/index.html"),
                        Row.of(3, "SW1A 1AA", DATE_TIME.plusSeconds(36), "www.website.com/help.html"),
                        Row.of(4, "W1J 7NT", DATE_TIME.plusSeconds(3), "www.website.com/index.html"),
                        Row.of(5, "W1J 7NT", DATE_TIME.plusSeconds(8), "www.website.com/index.html"),
                        Row.of(1, "SW1A 1AA", DATE_TIME.plusMinutes(1).plusSeconds(53), "www.website.com/index.html"),
                        Row.of(2, "SW1A 1AA", DATE_TIME.plusMinutes(1).plusSeconds(32), "www.website.com/index.html"),
                        Row.of(3, "W1J 7NT", DATE_TIME.plusMinutes(1).plusSeconds(31), "www.website.com/index.html"),
                        Row.of(4, "W1J 7NT", DATE_TIME.plusMinutes(1).plusSeconds(19), "www.website.com/index.html"),
                        Row.of(5, "W1J 7NT", DATE_TIME.plusMinutes(1).plusSeconds(42), "www.website.com/index.html"));

        AggregationPipeline pipeline = new AggregationPipeline("unused", "unused");
        TableResult results = pipeline.passThrough(views).execute();

        List<Row> materialised = materialize(results);

        MatcherAssert.assertThat(
                materialised,
                Matchers.containsInAnyOrder(
                        Row.of(1, "SW1A 1AA", DATE_TIME.plusSeconds(5), "www.website.com/faq.html", "2020-01-01", "00"),
                        Row.of(2, "SW1A 1AA", DATE_TIME.plusSeconds(47), "www.website.com/index.html", "2020-01-01", "00"),
                        Row.of(3, "SW1A 1AA", DATE_TIME.plusSeconds(36), "www.website.com/help.html", "2020-01-01", "00"),
                        Row.of(4, "W1J 7NT", DATE_TIME.plusSeconds(3), "www.website.com/index.html", "2020-01-01", "00"),
                        Row.of(5, "W1J 7NT", DATE_TIME.plusSeconds(8), "www.website.com/index.html", "2020-01-01", "00"),
                        Row.of(1, "SW1A 1AA", DATE_TIME.plusMinutes(1).plusSeconds(53), "www.website.com/index.html", "2020-01-01", "00"),
                        Row.of(2, "SW1A 1AA", DATE_TIME.plusMinutes(1).plusSeconds(32), "www.website.com/index.html", "2020-01-01", "00"),
                        Row.of(3, "W1J 7NT", DATE_TIME.plusMinutes(1).plusSeconds(31), "www.website.com/index.html", "2020-01-01", "00"),
                        Row.of(4, "W1J 7NT", DATE_TIME.plusMinutes(1).plusSeconds(19), "www.website.com/index.html", "2020-01-01", "00"),
                        Row.of(5, "W1J 7NT", DATE_TIME.plusMinutes(1).plusSeconds(42), "www.website.com/index.html", "2020-01-01", "00")));

    }

    /**
     * Test that the aggregation logic correctly buckets the data.
     */
    @Test
    @SneakyThrows
    public void testAggregation() {
        EnvironmentSettings settings = EnvironmentSettings.newInstance().inBatchMode().build();
        TableEnvironment tEnv = TableEnvironment.create(settings);

        Table views =
                tEnv.fromValues(
                        DataTypes.ROW(
                                DataTypes.FIELD("user_id", DataTypes.INT().notNull()),
                                DataTypes.FIELD("postcode", DataTypes.STRING().notNull()),
                                DataTypes.FIELD("timestamp", DataTypes.TIMESTAMP(3)),
                                DataTypes.FIELD("webpage", DataTypes.STRING().notNull())),
                        Row.of(1, "SW1A 1AA", DATE_TIME.plusSeconds(5), "www.website.com/index.html"),
                        Row.of(2, "SW1A 1AA", DATE_TIME.plusSeconds(47), "www.website.com/index.html"),
                        Row.of(3, "SW1A 1AA", DATE_TIME.plusSeconds(36), "www.website.com/index.html"),
                        Row.of(4, "W1J 7NT", DATE_TIME.plusSeconds(3), "www.website.com/index.html"),
                        Row.of(5, "W1J 7NT", DATE_TIME.plusSeconds(8), "www.website.com/index.html"),
                        Row.of(1, "SW1A 1AA", DATE_TIME.plusMinutes(1).plusSeconds(53), "www.website.com/index.html"),
                        Row.of(2, "SW1A 1AA", DATE_TIME.plusMinutes(1).plusSeconds(32), "www.website.com/index.html"),
                        Row.of(3, "W1J 7NT", DATE_TIME.plusMinutes(1).plusSeconds(31), "www.website.com/index.html"),
                        Row.of(4, "W1J 7NT", DATE_TIME.plusMinutes(1).plusSeconds(19), "www.website.com/index.html"),
                        Row.of(5, "W1J 7NT", DATE_TIME.plusMinutes(1).plusSeconds(42), "www.website.com/index.html"));

        AggregationPipeline pipeline = new AggregationPipeline("unused", "unused");
        TableResult results = pipeline.aggregateViews(views).execute();


        MatcherAssert.assertThat(
                materialize(results),
                Matchers.containsInAnyOrder(
                        Row.of("SW1A 1AA", DATE_TIME.plusMinutes(1), 3L, "2020-01-01", "00"),
                        Row.of("SW1A 1AA", DATE_TIME.plusMinutes(2), 2L, "2020-01-01", "00"),
                        Row.of("W1J 7NT", DATE_TIME.plusMinutes(1), 2L, "2020-01-01", "00"),
                        Row.of("W1J 7NT", DATE_TIME.plusMinutes(2), 3L, "2020-01-01", "00")));

    }

    private static List<Row> materialize(TableResult results) {
        try (CloseableIterator<Row> resultIterator = results.collect()) {
            return StreamSupport
                    .stream(Spliterators.spliteratorUnknownSize(resultIterator, Spliterator.ORDERED), false)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new RuntimeException("Failed to materialize results", e);
        }
    }

}
