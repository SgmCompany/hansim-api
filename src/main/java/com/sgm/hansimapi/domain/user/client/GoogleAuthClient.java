package com.sgm.hansimapi.domain.user.client;

public interface GoogleAuthClient {

    GoogleUserInfo verify(String googleToken);
}
