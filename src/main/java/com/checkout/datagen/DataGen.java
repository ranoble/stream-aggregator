package com.checkout.datagen;

import org.apache.commons.text.StringSubstitutor;
import org.apache.flink.table.api.EnvironmentSettings;
import org.apache.flink.table.api.Table;
import org.apache.flink.table.api.TableEnvironment;
import org.apache.flink.table.functions.ScalarFunction;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.checkout.tables.Catalog.PAGE_VIEWS_GENERATION;
import static com.checkout.tables.Catalog.PAGE_VIEWS_STREAM;
import static java.lang.Math.abs;
import static org.apache.flink.table.api.Expressions.$;
import static org.apache.flink.table.api.Expressions.call;

public class DataGen {
    public static List<String> POSTCODES = Arrays.asList(
            "SW1A 1AA", "W1J 7NT", "EC1A 1BB", "NW1 5PN", "SE1 7DP", "SW1H 9EA",
            "W1W 8NQ", "NW1 9DJ", "SE1 1DP", "SW1P 3DD", "W1F 6DP", "NW1 1PE", "SE1 8RT", "SW1P 4DP",
            "W1G 6RL", "NW1 1PF", "SE1 9RT", "SW1R 4RG", "W1K 5DP", "NW1 1PJ"
    );
    public static List<String> PAGES = Arrays.asList(
            "www.website.com/index.html", "www.website.com/faq.html", "www.website.com/deals.html",
            "www.website.com/catalog.html", "www.website.com/blah.html"
    );

    public static void main(String[] args) throws Exception {
        EnvironmentSettings settings = EnvironmentSettings
                .newInstance()
                .inStreamingMode()
                .build();

        int rows = Integer.parseInt(Optional.ofNullable(System.getenv("ROWS_PER_SECOND")).orElse("1000"));
        int maxLateRecord = Integer.parseInt(Optional.ofNullable(System.getenv("MAX_LATE_RECORD")).orElse("1000"));
        String consumerGroup = Optional.ofNullable(System.getenv("CONSUMER_GROUP")).orElse("unused");
        String bootstrap = Optional.ofNullable(System.getenv("KAFKA_BOOTSTRAP")).orElse("kafka:9092");

        TableEnvironment tEnv = TableEnvironment.create(settings);

        tEnv.executeSql(StringSubstitutor.replace(
                PAGE_VIEWS_GENERATION, Map.of("ROWS_PER_SECOND", rows, "MAX_LATE_RECORD", maxLateRecord),
                "${", "}"));

        tEnv.executeSql(StringSubstitutor.replace(
                PAGE_VIEWS_STREAM, Map.of("CONSUMER_GROUP", consumerGroup, "KAFKA_BOOTSTRAP", bootstrap),
                "${", "}"));

        // Use a simple UDF so we can use the built in DataGen function to build our test data.
        Table generated = tEnv.from("PageViewDataGen").select($("user_id"),
                call(GenerateOption.class, $("postcode_index"), POSTCODES).as("postcode"),
                $("timestamp"),
                call(GenerateOption.class, $("webpage_index"), PAGES).as("webpage"));
        generated.insertInto("PageViews").execute();
    }

    public static class GenerateOption extends ScalarFunction {
        public String eval(int index, List<String> options) {
            return options.get(abs(index) % options.size());
        }
    }
}