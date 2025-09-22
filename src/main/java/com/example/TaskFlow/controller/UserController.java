package com.example.TaskFlow.controller;

import com.example.TaskFlow.Constants.ErrorCode;
import com.example.TaskFlow.dto.request.ChangePasswordDTO;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.repo.UserRepository;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserController(UserRepository userRepository,PasswordEncoder encoder){
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    /*
    @GetMapping("/all")
    public ResponseEntity<List<User>> getAll(){
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
    */

    @GetMapping("/me")
    public ResponseEntity<User> getMe(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new ResponseStatusException(ErrorCode.BAD_REQUEST.getStatus(),ErrorCode.BAD_REQUEST.getMessage());
        String username = (String)auth.getName();
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()){
            return ResponseEntity.ok(user.get());
        }else{
            throw new ResponseStatusException(ErrorCode.BAD_REQUEST.getStatus(),ErrorCode.BAD_REQUEST.getMessage());
        }
    }

    @PostMapping("/updatePassword")
    public ResponseEntity<?>  updatePassword(@RequestBody @Valid ChangePasswordDTO req ){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new ResponseStatusException(ErrorCode.BAD_REQUEST.getStatus(),ErrorCode.BAD_REQUEST.getMessage());
        String username = (String)auth.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if(userOpt.isPresent()){
            User user = userOpt.get();
            if(!encoder.matches(req.oldPassword().trim(),user.getPasswordHash())){
                log.info("User Change password request failed for username:{}", user.getUsername());
                throw new ResponseStatusException(ErrorCode.INVALID_PASSWORD.getStatus(), ErrorCode.INVALID_PASSWORD.getMessage());
            }
            user.setPasswordHash(encoder.encode(req.newPassword().trim()));
            userRepository.save(user);
            log.info("Password Successfully Updated for Username:{}", user.getUsername());
        }
        return ResponseEntity.ok("Password Updated Succesfully");
    }


}
