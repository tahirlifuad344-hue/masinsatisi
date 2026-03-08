package com.example.masinsatisi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TelebeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TelebeApplication.class, args);
    }

}

