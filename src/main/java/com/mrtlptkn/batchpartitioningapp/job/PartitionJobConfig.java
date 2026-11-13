package com.mrtlptkn.batchpartitioningapp.job;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.PartitionHandler;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.partition.support.TaskExecutorPartitionHandler;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class PartitionJobConfig {

    @Value("${batch.partition.grid-size:4}")
    private int gridSize; // Thread Havuzunun boyutu

    @Bean
    ThreadPoolTaskExecutor partitionTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(gridSize); // Thread sayısını belirledik.
        executor.setMaxPoolSize(gridSize); // Bizde partition sayısı gridSize sabitlediğimiz için maxPoolSize gridSize yaparız.
        // executor.setQueueCapacity(Integer.MAX_VALUE); // Default Davranış
        //executor.setQueueCapacity(0); // Eğer ThreadPoolda aktif hazır thread yoksa kuyrukalama yapma hata fırlat demek oluyor.
        executor.setThreadNamePrefix("partition-"); // Thread takibi için
        executor.initialize(); // Thread Start
        return executor;
    }

    // Bu bean parlacama işlemini üstlenir
    @Bean
    public Partitioner orderRangePartitioner(JdbcTemplate jdbcTemplate) {
        return new OrderRangePartioner(jdbcTemplate);
    }

    // MainStep Ana operasyonları çalıştıran step
    // Bide partitionlara bölümlenmiş workerStep var.
    // ana operasyonu bu bean çalıştıracak
    // ama bu bean partition bazlı mainStep üzerinden split edilecek
    @Bean
    public Step workerStep(JobRepository jobRepository,
                           PlatformTransactionManager transactionManager,
                           PartitionedOrderTasklet tasklet){
        return  new StepBuilder("workerStep", jobRepository)
                .tasklet(tasklet, transactionManager)
                .build();
    }

    // İlgili workerSteplerin parçalara ayrılmasından sorumlu handler
    @Bean
    public PartitionHandler partitionHandler(TaskExecutor partitionTaskExecutor, Step workerStep) {
        TaskExecutorPartitionHandler handler = new TaskExecutorPartitionHandler();
        handler.setTaskExecutor(partitionTaskExecutor);
        handler.setStep(workerStep); // workerStepleri partitionTaskExecutor kullanarak gridSize kadar parçalara ayıracak.
        handler.setGridSize(gridSize);
        return handler;

    }

    @Bean
    public Step partitionMainStep(JobRepository jobRepository,Partitioner orderRangePartitioner, PartitionHandler partitionHandler) {
        return new StepBuilder("partitionMainStep", jobRepository)
                .partitioner("workerStep", orderRangePartitioner)
                .partitionHandler(partitionHandler)
                .build();
    }


    // Ana süreç Job üzerin MainStep çağırarak oda workeStepleri farklı thread ayırarak süreci çalıştıracak
    @Bean
    public Job partitionJob(JobRepository jobRepository, Step partitionMainStep) {
        return new JobBuilder("partitionJob", jobRepository)
                .start(partitionMainStep)
                .build();
    }



}
