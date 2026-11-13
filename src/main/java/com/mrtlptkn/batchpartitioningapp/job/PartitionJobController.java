package com.mrtlptkn.batchpartitioningapp.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/partitions")
public class PartitionJobController {


    private final JobLauncher partitionJobLauncher;
    private final Job partitionJob;

    public PartitionJobController(JobLauncher partitionJobLauncher, Job partitionJob) {
        this.partitionJobLauncher = partitionJobLauncher;
        this.partitionJob = partitionJob;
    }





    @PostMapping("/run")
    public ResponseEntity runJob(@RequestParam("runDate") String runDate, @RequestParam("failPartition") String failPartition)
            throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {
        // Burada Spring Batch job'unu başlatmak için gerekli kodu ekleyebilirsiniz.
        // Örneğin, JobLauncher kullanarak job'u çalıştırabilirsiniz.

        var params = new JobParametersBuilder()
                .addString("runDate", runDate)
                .addString("failPartition", failPartition)
                .toJobParameters();

        JobExecution execution =  partitionJobLauncher.run(partitionJob, params);

        return ResponseEntity.ok("OK");
    }

}
