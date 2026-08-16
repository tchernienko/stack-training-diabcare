package com.example.diabcare.bootstrap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.example.diabcare")
public class DiabcareApplication {

    static void main(String[] args) {
        SpringApplication.run(DiabcareApplication.class, args);
    }

}
