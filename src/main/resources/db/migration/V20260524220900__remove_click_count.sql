-- We now use the 'url_clicks' table via Kafka for high-scale analytics
ALTER TABLE url_mappings DROP COLUMN click_count;