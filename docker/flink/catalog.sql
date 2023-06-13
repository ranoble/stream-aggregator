SET 'sql-client.execution.result-mode' = 'tableau';
SET 'execution.runtime-mode' = 'batch';

CREATE TABLE PageViewsAggregate (
    postcode STRING NOT NULL,
    `window` TIMESTAMP(3) NOT NULL,
    `views` BIGINT NOT NULL,
    `day` STRING NOT NULL, 
    `hour` STRING NOT NULL, 
    WATERMARK FOR `window` AS `window` - INTERVAL '5' SECOND) 
 PARTITIONED BY (`day`, `hour`)
 WITH (
    'connector' = 'filesystem',
    'path' = 'file:///tmp/data/page_view_aggregates',
    'sink.partition-commit.trigger'='process-time',
    'sink.rolling-policy.rollover-interval'='1 m',
    'sink.rolling-policy.file-size' = '1MB', 
    'auto-compaction' = 'true', 
    'format'    = 'json'
);
    
CREATE TABLE PageViewsFS (
    user_id INT NOT NULL,
    postcode STRING NOT NULL,
    `timestamp` TIMESTAMP(3),
    webpage STRING NOT NULL, 
    `day` STRING NOT NULL, 
    `hour` STRING NOT NULL, 
    WATERMARK FOR `timestamp` AS `timestamp` - INTERVAL '5' SECOND) 
 PARTITIONED BY (`day`, `hour`)
 WITH (
    'connector' = 'filesystem',
    'path' = 'file:///tmp/data/page_views',
    'sink.partition-commit.trigger'='process-time',
    'sink.rolling-policy.rollover-interval'='1 m',
    'sink.rolling-policy.file-size' = '1MB', 
    'auto-compaction' = 'true', 
    'format' = 'json'
);

CREATE TABLE PageViews (
    user_id INT NOT NULL,
    postcode STRING NOT NULL,
    `timestamp` TIMESTAMP(3),
    webpage STRING NOT NULL,
    WATERMARK FOR `timestamp` AS `timestamp` - INTERVAL '5' SECOND)
WITH (
    'connector' = 'kafka',
    'topic'     = 'page_views',
    'properties.bootstrap.servers' = 'kafka:9092',
    'scan.startup.mode' = 'earliest-offset',
    'properties.group.id' = 'sql',
    'format'    = 'json'
);