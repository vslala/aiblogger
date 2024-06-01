package com.aiblogger.workflows.articlegenerator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;
import java.io.IOException;

public class PromptGenerator implements Tasklet {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final JobVariables jobVariables;

    public PromptGenerator(JobVariables jobVariables) {
        this.jobVariables = jobVariables;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        System.out.println("Executing Prompt Generator Step...");
        JobParameters jobParameters = contribution.getStepExecution().getJobParameters();
        String promptFilePath = jobParameters.getString(jobVariables.promptFilePathKey());
        String customPrompt = generateCustomPrompt(promptFilePath);
        contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put(jobVariables.promptKey(), customPrompt);

        return RepeatStatus.FINISHED;
    }

    @SneakyThrows
    private String generateCustomPrompt(String promptFilePath) {
        assert promptFilePath != null;
        JsonNode json = objectMapper.readTree(new File(promptFilePath));
        return json.get("generateNewBlogPost").asText();
    }
}
