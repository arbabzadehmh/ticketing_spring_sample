package ir.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "ocrExecutor")
    public Executor ocrExecutor() {

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(6);
        executor.setMaxPoolSize(12);

        executor.setQueueCapacity(200);

        executor.setThreadNamePrefix("ocr-");

        executor.setRejectedExecutionHandler(
                new ThreadPoolExecutor.DiscardOldestPolicy()
        );

        executor.initialize();

        return executor;
    }
}
