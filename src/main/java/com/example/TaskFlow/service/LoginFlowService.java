package com.example.TaskFlow.service;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

// This class stores the FlowId's map which allows us to recognize which user is trying to Login
// It has a Concurrent HashMap called Store. Which stores all the flowIds againts their flow objects.

@Service
public class LoginFlowService {

    public static class Flow{
        public final String id;
        public final String username;
        public int attempts = 0;
        public Instant expiresAt;

        Flow (String id, String username, Instant expiresAt){
            this.id = id;
            this.username = username;
            this.expiresAt = expiresAt;
        }

    }

    private final Map<String,Flow> store = new ConcurrentHashMap<>();

    public Flow create(String username){
        String flowId = UUID.randomUUID().toString();
        Flow flow = new Flow(flowId,username,Instant.now().plusSeconds(300));
        store.put(flowId,flow);
        return flow;
    }

    public Optional<Flow> get(String flowId){
        Flow flow = store.get(flowId);
        if(flow == null || Instant.now().isAfter(flow.expiresAt)){
            return Optional.empty();
        }
        return Optional.of(flow);
    }

    public void delete(String flowId){
        store.remove(flowId);
    }


}
