package com.aiblogger.workflows.articlegenerator;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PromptGeneratorTest {

    public static final JobVariables JOB_VARIABLES = new JobVariables("prompt", "promptFilePath", "llmModelId", "blogPostContent");
    public static final JobParameters JOB_PARAMS = new JobParametersBuilder()
            .addString("runId", UUID.randomUUID().toString())
            .addString("promptFilePath", "src/test/resources/test_custom_prompts.json")
            .addString("llmModelId", "anthropic.claude-3-sonnet-20240229-v1:0")
            .toJobParameters();

    @Test
    void should_generate_prompt_for_creating_new_blog_post() {
        // Given
        var promptGenerator = new PromptGenerator(JOB_VARIABLES);
        var stepContribution = new StepContribution(new StepExecution("step1", new JobExecution(1L, JOB_PARAMS)));
        ChunkContext chunkContext = new ChunkContext(new StepContext(new StepExecution("step1", new JobExecution(1L, JOB_PARAMS))));

        // When
        RepeatStatus repeatStatus = promptGenerator.execute(stepContribution, chunkContext);

        // Then
        assertEquals(RepeatStatus.FINISHED, repeatStatus);
        assertEquals("Please generate an engaging and informative blog post on a topic of your choice.", stepContribution.getStepExecution().getJobExecution().getExecutionContext().get("prompt"));
    }

}
