package com.api.smsapi.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "naver-cloud-sms")
public class SmsProperties {

    private String accessKey;

    private String secretKey;

    private String serviceId;

    private String senderPhone;

}
