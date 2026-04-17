package com.sgm.hansimapi.application.auth.command;

public class GoogleLoginCommand {

    private final String googleToken;

    private GoogleLoginCommand(String googleToken) {
        this.googleToken = googleToken;
    }

    public static GoogleLoginCommand of(String googleToken) {
        return new GoogleLoginCommand(googleToken);
    }

    public String getGoogleToken() { return googleToken; }
}
