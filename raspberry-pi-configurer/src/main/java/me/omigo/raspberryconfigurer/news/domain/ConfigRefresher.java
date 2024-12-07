package me.omigo.raspberryconfigurer.news.domain;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.omigo.raspberryconfigurer.config.Attributes;
import me.omigo.raspberryconfigurer.config.domain.ConfigService;
import me.omigo.raspberryconfigurer.config.query.ConfigQueryDto;
import me.omigo.raspberryconfigurer.config.query.ConfigQueryService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ConfigRefresher {

    private ConfigQueryDto configQueryDto;
    private final ConfigQueryService service;

    @PostConstruct
    void init() {
        configQueryDto = service.getConfigDto();
    }

    @EventListener
    public void handleMyCustomEvent(ConfigService.ConfigReloadEvent event) {
        log.info("Received config reload event");
        configQueryDto = service.getConfigDto();
    }

    public String getAttribute(Attributes attributes) {
        return configQueryDto.attributes().get(attributes.getKey());
    }
}
