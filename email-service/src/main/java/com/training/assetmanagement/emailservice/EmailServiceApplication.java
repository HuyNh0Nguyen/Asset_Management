package com.training.assetmanagement.emailservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
public class EmailServiceApplication {

    private static final String BUSINESS_TIME_ZONE = "Asia/Ho_Chi_Minh";

    public static void main(String[] args) {
        // Cùng múi giờ với backend
        TimeZone.setDefault(TimeZone.getTimeZone(BUSINESS_TIME_ZONE));
        SpringApplication.run(EmailServiceApplication.class, args);
    }

}
