package com.garage.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class GarageManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(GarageManagementApplication.class, args);
    }
}
