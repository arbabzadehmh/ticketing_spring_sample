package ir.config;

import ir.service.SectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SectionCacheWarmup implements ApplicationListener<ApplicationReadyEvent> {

    private final SectionService sectionService;

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        sectionService.findAll();               // کش‌سازی اینجا به‌درستی فعال می‌شود
        sectionService.findAll(PageRequest.of(0, 50));
        sectionService.findAllForFilter();
        log.info("**************************************************************  cache sections  ***************************************************************");
    }

}
