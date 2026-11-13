package com.mrtlptkn.batchpartitioningapp.schedule;

import com.mrtlptkn.batchpartitioningapp.job.PartitionJobLauncher;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;

@Component
@ConditionalOnProperty(name = "batch.schedule.enabled", havingValue = "true")
public class PartitionJobScheduler {

    private final PartitionJobLauncher partitionJobLauncher;
    private final ZoneId ISTANBUL = ZoneId.of("Europe/Istanbul");

    public PartitionJobScheduler(PartitionJobLauncher partitionJobLauncher) {
        this.partitionJobLauncher = partitionJobLauncher;
    }

    @Scheduled(cron = "${batch.schedule.cron}", zone = "Europe/Istanbul")
    public void runPartitionJob() throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        // Burada partition job'ı başlatmak için gerekli kodu ekleyebilirsiniz.
        // Örneğin, JobLauncher kullanarak job'u çalıştırabilirsiniz.

        // Bu runDate elimizde hergüne ait veri olmadığından dolayı bu şekilde sabit yazıldı normalde
        // normal değer -> LocalDate.now(ISTANBUL).toString();
        String runDate = LocalDate.of(2026,1,15).toString();
        partitionJobLauncher.run(runDate,"none");
    }


}
