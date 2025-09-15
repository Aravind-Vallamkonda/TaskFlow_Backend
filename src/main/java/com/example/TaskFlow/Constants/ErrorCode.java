package com.example.TaskFlow.Constants;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    USER_ALREADY_EXISTS("User already exists", HttpStatus.CONFLICT),
    INVALID_PASSWORD ("Invalid Password",HttpStatus.UNAUTHORIZED),
    USER_NOT_FOUND("User not found.",HttpStatus.NOT_FOUND),
    INVALID_FLOWID("Invalid FlowId",HttpStatus.BAD_REQUEST),
    MAXIMUM_PASSWORD_ATTEMPTS_REACHED("Maximum Password Attempts Reached.",HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status){
        this.message = message;
        this.status = status;
    }
}
