package com.aiblogger.workflows.articlegenerator;

import com.aiblogger.workflows.configurations.WordpressApiConfig;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class ArticleGeneratorJob {

    public static final JobVariables JOB_VARIABLES = new JobVariables(
            "prompt",
            "promptFilePath",
            "llmModelId",
            "blogPostContent",
            "transformedBlocksContent"
    );

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final WordpressApiConfig wordpressApiConfig;

    @Autowired
    public ArticleGeneratorJob(JobRepository jobRepository, PlatformTransactionManager transactionManager, WordpressApiConfig wordpressApiConfig) {
        this.jobRepository = jobRepository;
        this.transactionManager = transactionManager;
        this.wordpressApiConfig = wordpressApiConfig;
    }

    @Bean("articleGeneratorWorkFlowJob")
    public Job articleGeneratorWorkFlowJob() {
        return new JobBuilder("articleGeneratorJob", jobRepository)
                .start(getGeneratePromptStep())
                .next(invokeClaudeGenerateBlogArticleStep())
                .next(transformContentToBlocks())
                .next(publishBlogPost())
                .build();
    }

    private Step publishBlogPost() {
        return new StepBuilder("Publish blog post", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new PublishBlogPost(JOB_VARIABLES, wordpressApiConfig), transactionManager)
                .build();
    }

    private Step transformContentToBlocks() {
        return new StepBuilder("Transform Blog Article Content to Blocks", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new TransformContentToBlocks(JOB_VARIABLES), transactionManager)
                .build();
    }

    private Step getGeneratePromptStep() {
        return new StepBuilder("Prompt Generator Step", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new PromptGenerator(JOB_VARIABLES), transactionManager)
                .build();

    }

    private Step invokeClaudeGenerateBlogArticleStep() {
        return new StepBuilder("Invoke Claude Step", jobRepository)
                .allowStartIfComplete(true)
                .tasklet(new InvokeClaudeGenerateBlogArticle(JOB_VARIABLES), transactionManager)
                .build();
    }
}
