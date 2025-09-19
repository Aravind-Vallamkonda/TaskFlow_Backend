package com.example.TaskFlow.controller;

import com.example.TaskFlow.Constants.ErrorCode;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.repo.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/user")
public class UserController {

    private final UserRepository userRepository;

    public UserController(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    @GetMapping("/all")
    public ResponseEntity<List<User>> getAll(){
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

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

    @GetMapping("/updatePassword")
    public ResponseEntity<?>  updatePassword(){
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) throw new ResponseStatusException(ErrorCode.BAD_REQUEST.getStatus(),ErrorCode.BAD_REQUEST.getMessage());
        String username = (String)auth.getName();
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isPresent()){

        }
        return ResponseEntity.ok().build();
    }


}
