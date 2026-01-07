package com.talentica.payment.payment_service.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "authorize-net")
public class AuthorizeNetProperties {

    private String apiLoginId;
    private String transactionKey;
    private boolean sandbox;
}

