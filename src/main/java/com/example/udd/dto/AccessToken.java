package com.example.udd.dto;

public class AccessToken {
    public Long id;
    public String accessToken;

    public AccessToken(Long id, String accessToken) {
        this.id = id;
        this.accessToken = accessToken;
    }
}
