package com.aihub;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@EnableConfigurationProperties
@ComponentScan(basePackages = {"com.aihub", "com.aihub.admin", "com.aihub.common", "com.aihub.ai", "com.aihub.applications"})
public class AihubApplication {

    public static void main(String[] args) {
        SpringApplication.run(AihubApplication.class, args);
    }
}
