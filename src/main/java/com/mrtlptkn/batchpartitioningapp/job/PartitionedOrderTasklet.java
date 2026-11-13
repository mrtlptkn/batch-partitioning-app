package com.mrtlptkn.batchpartitioningapp.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.JdbcUpdateAffectedIncorrectNumberOfRowsException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

// Partition Bazlı execute edilecekler

@Component
@StepScope
public class PartitionedOrderTasklet implements Tasklet {

    private final Logger log = LoggerFactory.getLogger(PartitionedOrderTasklet.class);

    @Value("#{stepExecutionContext['minId']}")
    private Long minId;

    @Value("#{stepExecutionContext['maxId']}")
    private Long maxId;

    @Value("#{stepExecutionContext['partitionName']}")
    private String partitionName;

    // Job seviyesinde gün bazlı runDate ile ilgili bir kısım
    // On gün için bir rapor hesaplama özeti yapabilir
    @Value("#{jobParameters['runDate']}")
    private String runDate;

    private final JdbcTemplate jdbcTemplate;

    public PartitionedOrderTasklet(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("Partition -> {}, Thread Id-> {}, minId -> {}, maxId -> {}" , partitionName, Thread.currentThread().getName(),minId,maxId);

        try {

            // Daha önceki kayıtları silmek lazım -> Eksik
           jdbcTemplate.update(
                    "DELETE FROM customer_totals WHERE run_date = CAST(? AS DATE) AND partition_name = ?", runDate, partitionName);

            // Rapor oluşturma
            int written = jdbcTemplate.update("INSERT INTO customer_totals (run_date, customer, partition_name, total_amount) " + "SELECT CAST(? AS DATE), customer, CAST(? AS VARCHAR(50)), SUM(amount) " + "FROM orders WHERE order_id BETWEEN ? AND ? " + "AND order_date = CAST(? AS DATE) GROUP BY customer",runDate,partitionName,minId,maxId,runDate);

            // Sayaçlar manuel
            contribution.incrementWriteCount(written); // Sayaçların ne kadar kayıt işlediği

        } catch (RuntimeException e) {
            throw new JdbcUpdateAffectedIncorrectNumberOfRowsException("Beklenmey;en bir hata oluştu", 1, 0);
        }

        return RepeatStatus.FINISHED;
    }
}
