package com.tricore.crmtoyota;

import com.tricore.crmtoyota.config.CrmProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(CrmProperties.class)
@EnableScheduling
public class CrmToyotaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CrmToyotaApplication.class, args);
    }
}
