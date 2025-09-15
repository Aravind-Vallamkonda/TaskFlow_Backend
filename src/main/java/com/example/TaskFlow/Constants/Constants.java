package com.example.TaskFlow.Constants;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public class Constants {

    public static final String REFRESH_TOKEN_CLAIM = "refresh-token";
    public static final String ACCESS_TOKEN_CLAIM = "access-token";
    public static final String ROLES = "roles";
    public static final String TYPE = "type";
    public static final String BEARER_PREFIX = "Bearer ";

    public static ResponseEntity<Map<String,String>> invalid(String reason){
        String message = "invalid " + reason;
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error",message));
    }

}
