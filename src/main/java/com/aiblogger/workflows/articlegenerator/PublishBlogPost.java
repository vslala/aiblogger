package com.aiblogger.workflows.articlegenerator;

import com.aiblogger.workflows.configurations.WordpressApiConfig;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PublishBlogPost implements Tasklet {

    private final JobVariables jobVariables;
    private final WordpressApiConfig wordpressApiConfig;

    public PublishBlogPost(JobVariables jobVariables, WordpressApiConfig wordpressApiConfig) {
        this.jobVariables = jobVariables;
        this.wordpressApiConfig = wordpressApiConfig;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(wordpressApiConfig.getUsername(), wordpressApiConfig.getPassword());
        headers.setContentType(MediaType.APPLICATION_JSON);

        JobParameters jobParameters = contribution.getStepExecution().getJobParameters();
        JobExecution jobExecution = contribution.getStepExecution().getJobExecution();

        String originalJsonContent = jobExecution.getExecutionContext().getString(jobVariables.blogPostContentKey());

        var blogContent = new JSONObject(
                parseJsonBlock(originalJsonContent)
        );
        var blockContent = parseBlocks(jobExecution.getExecutionContext().getString(jobVariables.transformedBlocksContentKey()));

        String title = blogContent.getString("title");
        String category = blogContent.getString("categories");
        String status = blogContent.getString("status");
        var blogPost = new BlogArticle(
            title, category, blockContent, status
        );

        HttpEntity<BlogArticle> requestEntity = new HttpEntity<>(blogPost, headers);

        // Make the POST request
        ResponseEntity<String> response = restTemplate.postForEntity("http://" + wordpressApiConfig.getHostName() + "/wp-json/wp/v2/posts", requestEntity, String.class);

        // Print the response
        System.out.println("Response Code: " + response.getStatusCode());
        System.out.println("Response Body: " + response.getBody());

        return RepeatStatus.FINISHED;
    }

    private String parseBlocks(String text) {
        // Regular expression to find the Python text block
        String regex = "(?s)```blocks\\s*(.*?)\\s*```";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            return text;
        }
    }

    private String parseJsonBlock(String text) {
        // Regular expression to find the Python text block
        String regex = "(?s)```json\\s*(.*?)\\s*```";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            return matcher.group(1).trim();
        } else {
            return "No JSON text block found.";
        }
    }

    private record BlogArticle(String title, String categories, String content, String status) {}
}
