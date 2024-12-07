package me.omigo.raspberryconfigurer.news.in;

import lombok.RequiredArgsConstructor;
import me.omigo.raspberryconfigurer.news.query.WeatherQueryDto;
import me.omigo.raspberryconfigurer.news.query.WeatherQueryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/weather")
@RequiredArgsConstructor
public class WeatherRestController {
    private final WeatherQueryService weatherQueryService;

    @GetMapping
    WeatherQueryDto getWeatherStatus() {
        return weatherQueryService.getWeatherStatus();
    }
}
