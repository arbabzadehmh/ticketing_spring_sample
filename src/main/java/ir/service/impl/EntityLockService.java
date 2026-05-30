package ir.service.impl;

import ir.controller.exception.EntityLockedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
public class EntityLockService {

    private final RedisTemplate<String, String> redisTemplate;

    public EntityLockService(
            @Qualifier("lockRedisTemplate")
            RedisTemplate<String, String> redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    private String key(String entity, Long id) {
        return "lock:" + entity + ":" + id;
    }

    private String key(String entity, String id) {
        return "lock:" + entity + ":" + id;
    }


    // START LOCK
    public void lock(String entity, Long id, String username) {

        try {

            String redisKey = key(entity, id);

            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, username, Duration.ofMinutes(2));

            if (Boolean.FALSE.equals(success)) {
                String lockedBy = redisTemplate.opsForValue().get(redisKey);

                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> start lock " + lockedBy);

                throw new EntityLockedException();
            }

        } catch (EntityLockedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis unavailable. Lock skipped.");
        }
    }

    public void lock(String entity, String id, String username) {

        try {

            String redisKey = key(entity, id);

            Boolean success = redisTemplate.opsForValue()
                    .setIfAbsent(redisKey, username, Duration.ofMinutes(2));

            if (Boolean.FALSE.equals(success)) {

                String lockedBy =
                        redisTemplate.opsForValue().get(redisKey);

                System.out.println(">>>>>>>> lock " + lockedBy);

                throw new EntityLockedException();
            }

        } catch (EntityLockedException e) {
            throw e;
        } catch (Exception e) {
            log.warn("Redis unavailable. Lock skipped.");
        }
    }

    // CHECK LOCK
    public String getLockOwner(String entity, Long id) {

        try {

            return redisTemplate.opsForValue().get(key(entity, id));

        } catch (Exception e){
            log.warn("Redis unavailable. Lock check skipped.");
            return null;
        }
    }

    public String getLockOwner(String entity, String id) {

        try {

            return redisTemplate.opsForValue().get(key(entity, id));

        }  catch (Exception e){
            log.warn("Redis unavailable. Lock check skipped.");
            return null;
        }
    }

    // UNLOCK
    public void unlock(String entity, Long id, String username) {

        try {

            String redisKey = key(entity, id);

            String owner = redisTemplate.opsForValue().get(redisKey);

            if (owner != null && owner.equals(username)) {
                redisTemplate.delete(redisKey);

                System.out.println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> unlock " + owner);


            }

        } catch (Exception e) {
            log.warn("Redis unavailable. Unlock skipped.");
        }
    }

    public void unlock(String entity, String id, String username) {

        try {

            String redisKey = key(entity, id);

            String owner =
                    redisTemplate.opsForValue().get(redisKey);

            if (owner != null && owner.equals(username)) {

                redisTemplate.delete(redisKey);

                System.out.println(">>>>>>>> unlock " + owner);
            }

        } catch (Exception e) {
            log.warn("Redis unavailable. Unlock skipped.");
        }
    }

}
