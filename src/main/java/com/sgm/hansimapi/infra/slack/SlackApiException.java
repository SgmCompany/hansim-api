package com.sgm.hansimapi.infra.slack;

public class SlackApiException extends RuntimeException {
    public SlackApiException(String message) {
        super(message);
    }
}
