package com.aiblogger.workflows.articlegenerator;

import com.aiblogger.workflows.llms.ClassicChatbot;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;

import java.util.Objects;

public class InvokeClaudeGenerateBlogArticle implements Tasklet {

    private final JobVariables jobVariables;

    public InvokeClaudeGenerateBlogArticle(JobVariables jobVariables) {
        this.jobVariables = jobVariables;
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        System.out.println("Invoking step: Invoke Claude!");

        ExecutionContext executionContext = contribution.getStepExecution()
                .getJobExecution()
                .getExecutionContext();
        String prompt = Objects.requireNonNull(executionContext.get(jobVariables.promptKey()))
                .toString();
        String llmModelId = contribution.getStepExecution().getJobParameters().getString(jobVariables.llmModelIdKey());

        var chatbot = new ClassicChatbot(llmModelId);
        String response = chatbot.input(prompt);
        executionContext.put(jobVariables.blogPostContentKey(), response);

        return RepeatStatus.FINISHED;
    }
}
