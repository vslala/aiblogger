package com.aiblogger.workflows;

import com.aiblogger.workflows.articlegenerator.ArticleGeneratorJob;
import org.springframework.batch.core.*;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class JobRunner {

    private final JobLauncher jobLauncher;
    private final Job articleGeneratorJob;

    @Autowired
    public JobRunner(JobLauncher jobLauncher, @Qualifier("articleGeneratorWorkFlowJob") Job articleGeneratorJob) {
        this.jobLauncher = jobLauncher;
        this.articleGeneratorJob = articleGeneratorJob;
    }

    @Scheduled(fixedDelay = 2, timeUnit = TimeUnit.MINUTES)
    public void run() {
        try {
            var jobParams = new JobParametersBuilder()
                    .addString("runId", UUID.randomUUID().toString())
                    .addString("promptFilePath", "src/main/resources/custom_prompts.json")
                    .addString("llmModelId", "anthropic.claude-3-sonnet-20240229-v1:0")
                    .toJobParameters();

            JobExecution jobExecution = jobLauncher.run(articleGeneratorJob, jobParams);
        } catch (JobExecutionAlreadyRunningException e) {
            throw new RuntimeException(e);
        } catch (JobRestartException e) {
            throw new RuntimeException(e);
        } catch (JobInstanceAlreadyCompleteException e) {
            throw new RuntimeException(e);
        } catch (JobParametersInvalidException e) {
            throw new RuntimeException(e);
        }

    }
}
