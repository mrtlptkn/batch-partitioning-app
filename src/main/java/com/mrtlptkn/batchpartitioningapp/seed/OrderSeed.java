package com.mrtlptkn.batchpartitioningapp.seed;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

// verilen bir adet random order bilgisi hazırlayıp veritabanına kaydeder.

@Service
public class OrderSeed {

    private static  final List<String> CUSTOMERS = List.of("Ahmet","Ayşe","Ali","Burak","Mustafa","Mert","Hasan","Hakan","Hande","Elif","Arzu");

    private final JdbcTemplate jdbcTemplate;


    public OrderSeed(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    public int Seed(int count) {
        this.jdbcTemplate.update("DELETE FROM customer_totals");
        this.jdbcTemplate.update("DELETE FROM orders");

        Random random = new Random();
        LocalDate startDate = LocalDate.of(2026, 1, 1);

        List<Object[]> rows = new ArrayList<>(count);

        for (long id = 1; id <= count; id++){
            String customer = CUSTOMERS.get(random.nextInt(CUSTOMERS.size()));
            BigDecimal amount = BigDecimal.valueOf(random.nextInt(250000), 2); //137.500
            LocalDate date = startDate.plusDays(random.nextInt(30)); // 30 gün gibi gidelim
            rows.add(new Object[]{id, customer, amount, date});

        }

        jdbcTemplate.batchUpdate(
                "INSERT INTO orders (order_id, customer, amount, order_date) VALUES (?, ?, ?, ?)", rows);

        return count;

    }

}
