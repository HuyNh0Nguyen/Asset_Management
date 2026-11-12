package com.training.assetmanagement.shared.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

// Lấy "bây giờ" qua Clock tiêm vào - không gọi LocalDate.now() rải rác
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        // Múi giờ mặc định đã đặt trong main()
        return Clock.systemDefaultZone();
    }
}
