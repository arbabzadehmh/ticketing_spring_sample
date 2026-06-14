package ir.service.impl;

import ir.controller.exception.EntityLockedException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EntityLockServiceTest {

    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    private EntityLockService entityLockService;


    @BeforeEach
    void setup() {
        when(redisTemplate.opsForValue())
                .thenReturn(valueOperations);

        entityLockService = new EntityLockService(redisTemplate);
    }

    @Test
    void lock_shouldCreateLock() {

        when(valueOperations.setIfAbsent(
                "lock:building:1",
                "admin",
                Duration.ofMinutes(2)
        )).thenReturn(true);


        entityLockService.lock(
                "building",
                1L,
                "admin"
        );


        verify(valueOperations)
                .setIfAbsent(
                        "lock:building:1",
                        "admin",
                        Duration.ofMinutes(2)
                );
    }

    @Test
    void lock_shouldThrowWhenAlreadyLocked() {

        when(valueOperations.setIfAbsent(
                anyString(),
                anyString(),
                any()
        )).thenReturn(false);

        when(valueOperations.get("lock:building:1"))
                .thenReturn("ali");


        assertThrows(
                EntityLockedException.class,
                () -> entityLockService.lock(
                        "building",
                        1L,
                        "admin"
                )
        );
    }

    @Test
    void lock_shouldIgnoreWhenRedisIsDown() {

        when(valueOperations.setIfAbsent(
                anyString(),
                anyString(),
                any()
        )).thenThrow(new RuntimeException());


        assertDoesNotThrow(() ->
                entityLockService.lock(
                        "building",
                        1L,
                        "admin"
                )
        );
    }

    @Test
    void getLockOwner_shouldReturnUsername() {

        when(valueOperations.get("lock:building:1"))
                .thenReturn("admin");


        String result =
                entityLockService.getLockOwner(
                        "building",
                        1L
                );


        assertEquals("admin", result);
    }

    @Test
    void getLockOwner_shouldReturnNullWhenRedisFails() {

        when(valueOperations.get(anyString()))
                .thenThrow(new RuntimeException());


        String result =
                entityLockService.getLockOwner(
                        "building",
                        1L
                );


        assertNull(result);
    }

    @Test
    void unlock_shouldDeleteLockWhenOwnerMatches() {

        when(valueOperations.get("lock:building:1"))
                .thenReturn("admin");


        entityLockService.unlock(
                "building",
                1L,
                "admin"
        );


        verify(redisTemplate)
                .delete("lock:building:1");
    }

    @Test
    void unlock_shouldNotDeleteWhenUserIsNotOwner() {

        when(valueOperations.get("lock:building:1"))
                .thenReturn("ali");


        entityLockService.unlock(
                "building",
                1L,
                "admin"
        );


        verify(redisTemplate, never())
                .delete(anyString());
    }

    @Test
    void unlock_shouldIgnoreRedisFailure() {

        when(valueOperations.get(anyString()))
                .thenThrow(new RuntimeException());


        assertDoesNotThrow(() ->
                entityLockService.unlock(
                        "building",
                        1L,
                        "admin"
                )
        );
    }

    @Test
    void lockWithStringId_shouldWork() {
        when(valueOperations.setIfAbsent(
                "lock:role:ROLE_ADMIN",
                "admin",
                Duration.ofMinutes(2)
        )).thenReturn(true);

        entityLockService.lock(
                "role",
                "ROLE_ADMIN",
                "admin"
        );

        verify(valueOperations)
                .setIfAbsent(
                        "lock:role:ROLE_ADMIN",
                        "admin",
                        Duration.ofMinutes(2)
                );
    }

}
