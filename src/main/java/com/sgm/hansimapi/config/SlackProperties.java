package com.sgm.hansimapi.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "slack")
public class SlackProperties {

    private String botToken;
    private Channel channel = new Channel();

    @Getter
    @Setter
    public static class Channel {
        private String general;
    }
}
