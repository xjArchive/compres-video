package com.spdbccc;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
class StartUpApplication {
    public static void main(String[] args) {
        SpringApplication.run(StartUpApplication.class, args);
    }
}
