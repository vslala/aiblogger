package com.aiblogger.workflows.articlegenerator;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class InvokeClaudeGenerateBlogArticleTest {

    public static final JobVariables JOB_VARIABLES = new JobVariables("prompt", "promptFilePath", "llmModelId", "blogPostContent");
    public static final JobParameters JOB_PARAMS = new JobParametersBuilder()
            .addString("runId", UUID.randomUUID().toString())
            .addString("promptFilePath", "src/test/resources/test_custom_prompts.json")
            .addString("llmModelId", "anthropic.claude-3-sonnet-20240229-v1:0")
            .toJobParameters();

    @Test
    void should_invoke_claude_pass_the_prompt_and_get_a_response() {
        var invokeClaude = new InvokeClaudeGenerateBlogArticle(JOB_VARIABLES);
        JobExecution jobExecution = new JobExecution(1L, JOB_PARAMS);
        ExecutionContext executionContext = new ExecutionContext();
        executionContext.put(JOB_VARIABLES.promptKey(), "Write a short blog post on a random title of your choice");
        jobExecution.setExecutionContext(executionContext);
        var stepContribution = new StepContribution(new StepExecution("step1", jobExecution));
        ChunkContext chunkContext = new ChunkContext(new StepContext(new StepExecution("step1", jobExecution)));

        invokeClaude.execute(stepContribution, chunkContext);

        String output = executionContext.getString(JOB_VARIABLES.blogPostContentKey());
        assertNotNull(output);

    }
}
