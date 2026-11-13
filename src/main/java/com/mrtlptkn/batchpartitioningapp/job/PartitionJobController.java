package com.mrtlptkn.batchpartitioningapp.job;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.concurrent.atomic.AtomicBoolean;

@RestController
@RequestMapping("api/v1/partitions")
public class PartitionJobController {


    private final JobLauncher partitionJobLauncher;
    private final Job partitionJob;
    private final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
    private final JobExplorer jobExplorer;
    private final Logger log = LoggerFactory.getLogger(PartitionJobController.class);

    public PartitionJobController(JobLauncher partitionJobLauncher, Job partitionJob, JobExplorer jobExplorer) {
        this.partitionJobLauncher = partitionJobLauncher;
        this.partitionJob = partitionJob;
        this.jobExplorer = jobExplorer;
    }



    @PostMapping("/run")
    public ResponseEntity<String> runJob(@RequestParam("runDate") String runDate, @RequestParam("failPartition") String failPartition)
            throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        // Kilit alınamazsa bu istek finally bloğuna girmez; kilidi sadece alan istek serbest bırakır.
        if (!atomicBoolean.compareAndSet(false, true)) {
            log.warn("Job zaten çalışıyor. Yeni bir job başlatılamaz.");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Job zaten çalışıyor.");
        }

        try {
            // Kilit bu process içinde alındı; veritabanında başka bir çalışan execution var mı kontrol et.
            if (!jobExplorer.findRunningJobExecutions("partitionJob").isEmpty()) {
                log.warn("partitionJob veritabanında zaten çalışıyor. Yeni job başlatılamadı.");
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Job zaten çalışıyor.");
            }

            var params = new JobParametersBuilder()
                    .addString("runDate", runDate)
                    .addString("failPartition", failPartition)
                    .toJobParameters();

            partitionJobLauncher.run(partitionJob, params);
        } finally {
            atomicBoolean.set(false);
        }

        return ResponseEntity.ok("OK");
    }

}
