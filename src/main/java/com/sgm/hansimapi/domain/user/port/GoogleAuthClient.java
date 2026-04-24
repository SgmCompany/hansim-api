package com.sgm.hansimapi.domain.user.port;

public interface GoogleAuthClient {

    GoogleUserInfo verify(String googleToken);
}
