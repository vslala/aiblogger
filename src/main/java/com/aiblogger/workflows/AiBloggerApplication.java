package com.aiblogger.workflows;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.batch.BatchAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties
@EnableScheduling
@EnableBatchProcessing
@SpringBootApplication
public class AiBloggerApplication {

	public static void main(String[] args) {
		SpringApplication.run(AiBloggerApplication.class, args);
	}

}
