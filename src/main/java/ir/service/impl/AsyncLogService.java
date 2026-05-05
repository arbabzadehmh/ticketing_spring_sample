package ir.service.impl;

import ir.model.entity.ApplicationLog;
import ir.repository.ApplicationLogRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
public class AsyncLogService {

    private final ApplicationLogRepository applicationLogRepository;

    public AsyncLogService(ApplicationLogRepository applicationLogRepository) {
        this.applicationLogRepository = applicationLogRepository;
    }

    @Async
    public void saveLog(
            String level,
            String loggerName,
            String message,
            String thread
    ) {

        try {

            ApplicationLog logEntity =
                    ApplicationLog.builder()
                            .logLevel(level)
                            .logger(loggerName)
                            .message(message)
                            .thread(thread)
                            .createdAt(LocalDateTime.now())
                            .build();

            applicationLogRepository.save(logEntity);

        } catch (Exception e) {

            log.error("Failed to save log into database", e);
        }
    }

}
