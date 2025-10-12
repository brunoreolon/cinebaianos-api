package com.brunoreolon.cinebaianosapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class CinebaianosApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinebaianosApiApplication.class, args);
    }

}
