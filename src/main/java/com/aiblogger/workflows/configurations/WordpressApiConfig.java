package com.aiblogger.workflows.configurations;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Data
@Configuration
@ConfigurationProperties(prefix = "wp")
public class WordpressApiConfig {
    private String hostName;
    private String username;
    private String password;


}
