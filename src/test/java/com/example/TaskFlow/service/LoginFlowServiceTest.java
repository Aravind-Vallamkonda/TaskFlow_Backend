package com.example.TaskFlow.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

public class LoginFlowServiceTest {

    private LoginFlowService service;

    @BeforeEach
    void setUp(){
        service = new LoginFlowService();
    }

    @Test
    void createShouldReturnUniqueFlowPerInvocation(){
        LoginFlowService.Flow first = service.create("alice");
        LoginFlowService.Flow second = service.create("alice");

        assertThat(first.id).isNotBlank();
        assertThat(second.id).isNotBlank();
        assertThat(first.id).isNotEqualTo(second.id);
        assertThat(first.username).isEqualTo("alice");
        assertThat(first.expiresAt).isAfter(Instant.now().minusSeconds(1));
        assertThat(service.get(first.id)).contains(first);
    }

    @Test
    void getShouldReturnEmptyWhenFlowExpires(){
        LoginFlowService.Flow flow = service.create("Bob");
        flow.expiresAt = Instant.now().minusSeconds(1);
        assertThat(service.get(flow.id)).isEmpty();
    }

    @Test
    void deleteShouldRemoveFlowFromStore(){
        LoginFlowService.Flow flow = service.create("Bob");
        service.delete(flow.id);
        assertThat(service.get(flow.id)).isEmpty();
    }

    @Test
    void createShouldRemainThreadSafeUnderConcurrency() throws Exception{
        ExecutorService executor = Executors.newFixedThreadPool(8);
        try{
            List<Callable<LoginFlowService.Flow>> tasks =
                    IntStream.range(0,100)
                            .mapToObj(i -> (Callable<LoginFlowService.Flow>) () ->service.create(("User" + i)))
                            .toList();
                    List<Future<LoginFlowService.Flow>> results = executor.invokeAll(tasks);
                    Set<String> ids = ConcurrentHashMap.newKeySet();

                    for(Future<LoginFlowService.Flow> future : results){
                        LoginFlowService.Flow flow = future.get(5,TimeUnit.SECONDS);
                        assertThat(flow).isNotNull();
                        ids.add(flow.id);
                    }
                    assertThat(ids.size()).isEqualTo(100);
        }finally{
            executor.shutdown();
            executor.awaitTermination(5, TimeUnit.SECONDS);
        }

    }

}
