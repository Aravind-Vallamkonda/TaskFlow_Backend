package com.example.TaskFlow.controller;

import org.apache.coyote.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @PostMapping("/identify")
    public ResponseEntity<?> identify(){
        return ResponseEntity.ok("Identify Endpoint");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(){
        return ResponseEntity.ok("Login Endpoint");
    }

}
