package me.omigo.raspberryconfigurer.config.in;

import lombok.RequiredArgsConstructor;
import me.omigo.raspberryconfigurer.config.command.ConfigCommandService;
import me.omigo.raspberryconfigurer.config.command.UpdateAttributesDTO;
import me.omigo.raspberryconfigurer.config.query.ConfigQueryService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/config")
@RequiredArgsConstructor
public class ConfigRestController {

    private final ConfigQueryService configQueryService;
    private final ConfigCommandService configCommandService;

    @PostMapping("/attributes")
    void addAttribute(@RequestBody UpdateAttributesDTO dto) {
        configCommandService.addAttribute(dto);
    }

    @DeleteMapping("/attributes/{key}")
    void removeAttribute(@PathVariable String key) {
        configCommandService.removeAttribute(key);
    }

    @GetMapping("/attributes")
    Map<String, String> getAttributes() {
        return configQueryService.getConfigDto().attributes();
    }

    @PostMapping("/wifi-mode")
    void tryConnectToNetwork() {
        configCommandService.tryToConnectToWifi();
    }
}
