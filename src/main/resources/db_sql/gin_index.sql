CREATE INDEX idx_parsed_product_name
ON parsed_product USING gin(to_tsvector('simple', name));
