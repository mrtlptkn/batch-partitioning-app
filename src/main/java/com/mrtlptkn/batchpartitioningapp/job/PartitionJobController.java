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


    private final PartitionJobLauncher partitionJobLauncher;

    public PartitionJobController(PartitionJobLauncher partitionJobLauncher) {
        this.partitionJobLauncher = partitionJobLauncher;
    }


    @PostMapping("/run")
    public ResponseEntity<String> runJob(@RequestParam("runDate") String runDate, @RequestParam("failPartition") String failPartition)
            throws JobInstanceAlreadyCompleteException, JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException {

        partitionJobLauncher.run(runDate, failPartition);

        return ResponseEntity.ok("OK");
    }

}
