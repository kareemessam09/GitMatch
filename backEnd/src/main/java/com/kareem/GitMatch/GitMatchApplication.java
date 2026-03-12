package com.kareem.GitMatch;

import com.kareem.GitMatch.config.GitMatchProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@EnableConfigurationProperties(GitMatchProperties.class)
public class GitMatchApplication {

	public static void main(String[] args) {
		SpringApplication.run(GitMatchApplication.class, args);
	}

}
