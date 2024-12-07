package me.omigo.raspberryconfigurer.config.domain;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationEmitter {

    private final ApplicationEventPublisher eventPublisher;

    @Async
    public void emitNotification() {
        log.info("Emitting config update event");
        eventPublisher.publishEvent(new ConfigService.ConfigReloadEvent());
        eventPublisher.publishEvent(new ConfigService.NewsWeatherReloadEvent());
    }
}
