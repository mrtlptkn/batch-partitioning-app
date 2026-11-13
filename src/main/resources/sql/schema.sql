-- Kaynak tablo: seed endpoint'i doldurur
CREATE TABLE IF NOT EXISTS orders (
    order_id   BIGINT PRIMARY KEY,
    customer   VARCHAR(100) NOT NULL,
    amount     DECIMAL(12,2) NOT NULL,
    order_date DATE NOT NULL
);

-- Hedef tablo: her partition kendi satırlarını yazar.
-- Anahtarda partition_name olduğu için iki partition aynı satıra yazmaz (yarış koşulu yok).
CREATE TABLE IF NOT EXISTS customer_totals (
    run_date       DATE NOT NULL,
    customer       VARCHAR(100) NOT NULL,
    partition_name VARCHAR(50) NOT NULL,
    total_amount   DECIMAL(14,2) NOT NULL,
    PRIMARY KEY (run_date, customer, partition_name)
);
