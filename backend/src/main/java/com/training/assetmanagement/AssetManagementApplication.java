package com.training.assetmanagement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

import java.util.TimeZone;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AssetManagementApplication {

    // Múi giờ nghiệp vụ — nguồn duy nhất cho JVM, Clock dùng lại giá trị này
    private static final String BUSINESS_TIME_ZONE = "Asia/Ho_Chi_Minh";

    public static void main(String[] args) {
        // Đặt TRƯỚC khi Spring khởi động — chạy bằng IDE, Maven hay Docker đều giống nhau
        TimeZone.setDefault(TimeZone.getTimeZone(BUSINESS_TIME_ZONE));
        SpringApplication.run(AssetManagementApplication.class, args);
    }

}
