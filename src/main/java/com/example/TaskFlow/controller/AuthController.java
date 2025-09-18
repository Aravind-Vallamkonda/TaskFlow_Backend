package com.example.TaskFlow.controller;

import com.example.TaskFlow.Constants.ErrorCode;
import com.example.TaskFlow.dto.request.IdentifyRequestDTO;
import com.example.TaskFlow.dto.response.IdentifyResponseDTO;
import com.example.TaskFlow.dto.request.LoginRequestDTO;
import com.example.TaskFlow.dto.request.RegisterDTO;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.repo.UserRepository;
import com.example.TaskFlow.service.JwtService;
import com.example.TaskFlow.service.LoginFlowService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import com.example.TaskFlow.Constants.Constants;
import org.apache.coyote.Response;
import org.hibernate.resource.transaction.backend.jta.internal.synchronization.RegisteredSynchronization;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.support.Repositories;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.web.server.ResponseStatusException;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import java.time.Duration;
import java.util.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

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
        log.info("User Registeration Started with Username:{}", regRequest.username());
        String username = regRequest.username().trim();
        String email = regRequest.email().trim();
        if(userRepository.findByEmailOrUsername(email,username).isPresent()){
            throw new ResponseStatusException(ErrorCode.USER_ALREADY_EXISTS.getStatus(),ErrorCode.USER_ALREADY_EXISTS.getMessage());
        }
        User user = new User();
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstname(regRequest.firstname().trim());
        user.setLastname(regRequest.lastname().trim());
        user.setPasswordHash(encoder.encode(regRequest.password().trim()));
        userRepository.save(user);
        log.info("User Registeration Successful for username :{} and email : {}", user.getUsername(), user.getUsername());

        return ResponseEntity.ok("User Created");
    }

    @PostMapping("/identify")
    public ResponseEntity<IdentifyResponseDTO> identify(@RequestBody @Valid IdentifyRequestDTO identifyRequest) {
        log.info("User Identification Started for identifier:{}", identifyRequest.identifier());
        Optional<User> userOptional = userRepository.findByEmailOrUsername(identifyRequest.identifier(), identifyRequest.identifier());

        if(userOptional.isEmpty()){
            log.info("User failed for identifier:{}", identifyRequest.identifier());
            throw new ResponseStatusException(ErrorCode.USER_NOT_FOUND.getStatus(),ErrorCode.USER_NOT_FOUND.getMessage() + ": "+ identifyRequest.identifier());
        }

        IdentifyResponseDTO identifyResponseDTO = new IdentifyResponseDTO();
        String username = userOptional.get().getUsername();
        var flow = loginFlowService.create(username);
        identifyResponseDTO.setFlowId(flow.id);
        log.info("User Identification successful for identifier:{}", identifyRequest.identifier());
        return ResponseEntity.ok(identifyResponseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO){

        var flowOpt = loginFlowService.get(loginRequestDTO.flowId());
        if(flowOpt.isEmpty()) throw new ResponseStatusException(ErrorCode.INVALID_FLOWID.getStatus(),ErrorCode.INVALID_FLOWID.getMessage());
        var flow = flowOpt.get();
        if(++flow.attempts > 3) throw new ResponseStatusException(ErrorCode.MAXIMUM_PASSWORD_ATTEMPTS_REACHED.getStatus(),ErrorCode.MAXIMUM_PASSWORD_ATTEMPTS_REACHED.getMessage());

        var uOpt = (flow.username !=null) ? userRepository.findByUsername(flow.username).orElse(null) : null;
        if(uOpt ==null) throw new ResponseStatusException(ErrorCode.USER_NOT_FOUND.getStatus(),ErrorCode.USER_NOT_FOUND.getMessage());
        log.info("User Login started for username:{}", uOpt.getUsername());
        if(!encoder.matches(loginRequestDTO.password().trim() , uOpt.getPasswordHash())) {
            log.info("User Login failed for username:{}", uOpt.getUsername());
            throw new ResponseStatusException(ErrorCode.INVALID_PASSWORD.getStatus(), ErrorCode.INVALID_PASSWORD.getMessage());
        }
        var roles = java.util.List.of("USER");
        String access = jwtService.createAccessToken(uOpt.getUsername(),roles);
        String refresh = jwtService.createRefreshToken(uOpt.getUsername(),roles);
        long refreshMaxAge = Duration.ofMinutes(jwtService.getRefreshTokenValidity()).getSeconds();
        ResponseCookie responseCookie = ResponseCookie.from(Constants.REFRESH_TOKEN_COOKIE,refresh)
                        .httpOnly(true)
                                .secure(false)
                                        .path("")
                                                .maxAge(refreshMaxAge)
                                                        .build();
        log.info("User Login successful for username:{}", uOpt.getUsername());
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, responseCookie.toString())
                .body(Map.of(Constants.ACCESS_TOKEN_CLAIM,access));
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {
        // We get all the Cookies stored in the request
        Cookie[] cookies = request.getCookies();
        if(cookies == null || cookies.length == 0){
            log.info("Refresh Token cookie missing from request.");
            throw new ResponseStatusException(ErrorCode.BAD_REQUEST.getStatus(),ErrorCode.BAD_REQUEST.getMessage());
        }

        String refreshToken = Arrays.stream(cookies)
                .filter(cookie -> Constants.REFRESH_TOKEN_COOKIE.equals(cookie.getName()))
                .map(Cookie::getValue)
                .filter(token -> token != null && !token.isEmpty())
                .findFirst()
                .orElse(null);

        if(refreshToken == null){
            log.info("Refresh Token cookie missing from request.");
            throw new ResponseStatusException(ErrorCode.INVALID_REFRESH_TOKEN.getStatus(),ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }

        var claims = jwtService.parse((refreshToken));
        if(jwtService.isTokenExpired(claims) || !jwtService.isRefreshToken(claims)){
            log.info("Refresh Token cookie has  expired from request.");
            throw new  ResponseStatusException(ErrorCode.INVALID_REFRESH_TOKEN.getStatus(),ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }

        String username =jwtService.extractUsername(claims);
        if(username == null || username.isBlank()){
            log.info("Refresh Token cookie missing username from request.");
            throw new ResponseStatusException(ErrorCode.INVALID_REFRESH_TOKEN.getStatus(),ErrorCode.INVALID_REFRESH_TOKEN.getMessage());
        }

        List<String> roles = jwtService.roles(claims);
        if(roles == null || roles.isEmpty()){
            roles = List.of("USER");
        }

        String access = jwtService.createAccessToken(username,roles);
        log.info("Access Token refreshed for username {}",username);
        return ResponseEntity.ok().body(Map.of(Constants.ACCESS_TOKEN_CLAIM,access));
    }

}
