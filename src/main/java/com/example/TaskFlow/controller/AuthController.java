package com.example.TaskFlow.controller;

import com.example.TaskFlow.dto.IdentifyRequestDTO;
import com.example.TaskFlow.dto.IdentifyResponseDTO;
import com.example.TaskFlow.dto.LoginRequestDTO;
import com.example.TaskFlow.model.User;
import com.example.TaskFlow.repo.UserRepository;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/identify")
    public ResponseEntity<IdentifyResponseDTO> identify(@RequestBody @Valid IdentifyRequestDTO identifyRequestDTO) {

        Optional<User> userOptional = userRepository.findByEmailOrUsername(identifyRequestDTO.getIdentifier(), identifyRequestDTO.getIdentifier());

        if(userOptional.isEmpty()){
            return ResponseEntity.badRequest().build();
        }

        IdentifyResponseDTO identifyResponseDTO = new IdentifyResponseDTO();
        identifyResponseDTO.setUsername(userOptional.get().getUsername());
        String flowId = UUID.randomUUID().toString();
        identifyResponseDTO.setFlowId(flowId);

        return ResponseEntity.ok(identifyResponseDTO);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid LoginRequestDTO loginRequestDTO){
        return ResponseEntity.ok("Login Endpoint");
    }

}
