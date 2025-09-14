package com.example.TaskFlow.controller;

import com.example.TaskFlow.dto.IdentifyRequestDTO;
import com.example.TaskFlow.dto.IdentifyResponseDTO;
import com.example.TaskFlow.dto.LoginRequestDTO;
import com.example.TaskFlow.dto.RegisterDTO;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.repo.UserRepository;
import com.example.TaskFlow.service.JwtService;
import com.example.TaskFlow.service.LoginFlowService;
import jakarta.validation.Valid;
import com.example.TaskFlow.Constants;

import org.apache.tomcat.util.bcel.Const;
import org.apache.tomcat.util.bcel.classfile.Constant;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.beans.Encoder;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final LoginFlowService loginFlowService;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(UserRepository userRepository,LoginFlowService loginFlowService, JwtService jwtService, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.loginFlowService = loginFlowService;
        this.jwtService = jwtService;
        this.encoder = encoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody @Valid RegisterDTO regRequest){

        String username = regRequest.getUsername().trim();
        String email = regRequest.getEmail().trim();
        if(userRepository.findByEmailOrUsername(email,username).isPresent()){
            return Constants.invalid("User already exists.");
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstname(regRequest.getFirstname().trim());
        user.setLastname(regRequest.getLastname().trim());
        user.setPasswordHash(encoder.encode(regRequest.getPassword().trim()));
        userRepository.save(user);

        return ResponseEntity.ok("User Created");
    }

    @PostMapping("/identify")
    public ResponseEntity<IdentifyResponseDTO> identify(@RequestBody @Valid IdentifyRequestDTO identifyRequestDTO) {

        Optional<User> userOptional = userRepository.findByEmailOrUsername(identifyRequestDTO.getIdentifier(), identifyRequestDTO.getIdentifier());

        if(userOptional.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        IdentifyResponseDTO identifyResponseDTO = new IdentifyResponseDTO();
        String username = userOptional.get().getUsername();
      //  identifyResponseDTO.setUsername(username);
        var flow = loginFlowService.create(username);
        identifyResponseDTO.setFlowId(flow.id);

        return ResponseEntity.ok(identifyResponseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO){
        var flowOpt = loginFlowService.get(loginRequestDTO.getFlowId());
        if(flowOpt.isEmpty()){
            return Constants.invalid("FlowId is empty");
        }
        var flow = flowOpt.get();
        if(++flow.attempts > 3) return Constants.invalid("Maximum Password attempts exceeded.");

        var uOpt = (flow.username !=null) ? userRepository.findByUsername(flow.username).orElse(null) : null;
        if(uOpt ==null) return Constants.invalid("User not Found");

        System.out.println("password recieved is: "+loginRequestDTO.getPassword());
        System.out.println("password Hash : "+encoder.encode(loginRequestDTO.getPassword()));
        System.out.println("password Hash : "+encoder.encode(loginRequestDTO.getPassword().trim()));
        System.out.println("Stored password Hash : "+uOpt.getPasswordHash());

        if(!encoder.matches(loginRequestDTO.getPassword().trim() , uOpt.getPasswordHash()))
            return Constants.invalid("Invalid Password");

        var roles = java.util.List.of("USER");
        String access = jwtService.createAccessToken(uOpt.getUsername(),roles);
        String refresh = jwtService.createRefreshToken(uOpt.getUsername(),roles);
        return ResponseEntity.ok(Map.of(Constants.ACCESS_TOKEN_CLAIM,access ,Constants.REFRESH_TOKEN_CLAIM,refresh));
    }

}
