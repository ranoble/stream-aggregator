package com.checkout.tables;

public class Catalog {

    public static String PAGE_VIEWS_STREAM = "CREATE TABLE PageViews (\n" +
            "    user_id INT NOT NULL,\n" +
            "    postcode STRING NOT NULL,\n" +
            "    `timestamp` TIMESTAMP(3),\n" +
            "    webpage STRING NOT NULL,\n" +
            "    WATERMARK FOR `timestamp` AS `timestamp` - INTERVAL '5' SECOND)\n" +
            "WITH (\n" +
            "    'connector' = 'kafka',\n" +
            "    'topic'     = 'page_views',\n" +
            "    'properties.bootstrap.servers' = '${KAFKA_BOOTSTRAP}',\n" +
            "    'scan.startup.mode' = 'earliest-offset',\n" +
            "    'properties.group.id' = '${CONSUMER_GROUP}',\n" +
            "    'format'    = 'json'\n" +
            ")";

    public static String PAGE_VIEWS_FILESYSTEM = "CREATE TABLE PageViewsFS (\n" +
            "    user_id INT NOT NULL,\n" +
            "    postcode STRING NOT NULL,\n" +
            "    `timestamp` TIMESTAMP(3),\n" +
            "    webpage STRING NOT NULL, \n" +
            "    `day` STRING NOT NULL, \n" +
            "    `hour` STRING NOT NULL, \n" +
            "    WATERMARK FOR `timestamp` AS `timestamp` - INTERVAL '5' SECOND) \n" +
            " PARTITIONED BY (`day`, `hour`) \n " +
            " WITH (\n" +
            "    'connector' = 'filesystem',\n" +
            "    'path' = 'file:///tmp/data/page_views',\n" +
            "    'sink.partition-commit.trigger'='process-time',\n" +
            "    'sink.rolling-policy.rollover-interval'='1 m',\n" +
            "    'sink.rolling-policy.file-size' = '1MB', \n" +
            "    'auto-compaction' = 'true', \n" +
            "    'format' = 'json'\n" +
            ")";


    public static String PAGE_VIEW_AGGREGATE = "CREATE TABLE PageViewsAggregate (\n" +
            "    postcode STRING NOT NULL,\n" +
            "    `window` TIMESTAMP(3) NOT NULL,\n" +
            "    `views` BIGINT NOT NULL,\n" +
            "    `day` STRING NOT NULL, \n" +
            "    `hour` STRING NOT NULL, \n" +
            "    WATERMARK FOR `window` AS `window` - INTERVAL '5' SECOND) \n" +
            " PARTITIONED BY (`day`, `hour`) \n " +
            " WITH (\n" +
            "    'connector' = 'filesystem',\n" +
            "    'path' = 'file:///tmp/data/page_view_aggregates',\n" +
            "    'sink.partition-commit.trigger'='process-time',\n" +
            "    'sink.rolling-policy.rollover-interval'='1 m',\n" +
            "    'sink.rolling-policy.file-size' = '1MB', \n" +
            "    'auto-compaction' = 'true', \n" +
            "    'format'    = 'json'\n" +
            ")";

    public static String PAGE_VIEWS_GENERATION = "CREATE TABLE PageViewDataGen (\n" +
            "    user_id INT NOT NULL,\n" +
            "    postcode_index INT NOT NULL,\n" +
            "    `timestamp` TIMESTAMP(3),\n" +
            "    webpage_index INT NOT NULL,\n" +
            "    WATERMARK FOR `timestamp` AS `timestamp` - INTERVAL '5' SECOND)\n" +
            "WITH (\n" +
            "    'connector' = 'datagen',\n" +
            "    'fields.timestamp.max-past' = '${MAX_LATE_RECORD}',\n" +
            "    'fields.timestamp.kind' = 'random',\n" +
            "    'fields.user_id.kind' = 'random',\n" +
            "    'fields.user_id.min' = '0',\n" +
            "    'fields.user_id.max' = '1000',\n" +
            "    'rows-per-second' = '${ROWS_PER_SECOND}'\n" +
            ")";
}
