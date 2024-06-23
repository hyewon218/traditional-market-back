package com.market.domain.kakaoPay.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("kakaopay")
public class KakaoPayProperties {

    private String secret_key;
    private String cid;
}
