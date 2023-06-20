package com.olympus.olympus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
public class OlympusApplication {

    public static void main(String[] args) {
        SpringApplication.run(OlympusApplication.class, args);
    }

}
