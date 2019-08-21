package com.agriculture.nct;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class NctApplication {
    public static void main(String[] args) {
        SpringApplication.run(NctApplication.class, args);
    }
}
