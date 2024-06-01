package com.aiblogger.workflows.articlegenerator;

import com.aiblogger.workflows.llms.ClassicChatbot;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.io.File;

public class TransformContentToBlocks implements Tasklet {

    private final static ObjectMapper objectMapper = new ObjectMapper();
    private final JobVariables jobVariables;

    public TransformContentToBlocks(JobVariables jobVariables) {
        this.jobVariables = jobVariables;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        JobParameters jobParameters = contribution.getStepExecution().getJobParameters();
        ExecutionContext executionContext = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();
        String promptFilePath = jobParameters.getString(jobVariables.promptFilePathKey());
        String customPrompt = generateCustomPrompt(promptFilePath);
        String inputPrompt = customPrompt.replace("<<<Content>>>", executionContext.getString(jobVariables.blogPostContentKey()));

        var chatbot = new ClassicChatbot(jobParameters.getString(jobVariables.llmModelIdKey()));
        String blocksContent = chatbot.input(inputPrompt);

        executionContext.put(jobVariables.transformedBlocksContentKey(), blocksContent);
        return RepeatStatus.FINISHED;
    }

    @SneakyThrows
    private String generateCustomPrompt(String promptFilePath) {
        assert promptFilePath != null;
        JsonNode json = objectMapper.readTree(new File(promptFilePath));
        return json.get("transformContentToBlocks").asText();
    }
}
