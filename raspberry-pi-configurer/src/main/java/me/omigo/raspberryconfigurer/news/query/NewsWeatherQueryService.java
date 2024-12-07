package me.omigo.raspberryconfigurer.news.query;

import lombok.RequiredArgsConstructor;
import me.omigo.raspberryconfigurer.news.NewsRepository;
import me.omigo.raspberryconfigurer.news.WeatherRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
class NewsWeatherQueryService implements NewsQueryService, WeatherQueryService {
    private final NewsRepository newsRepository;
    private final WeatherRepository weatherRepository;

    @Override
    public List<NewsQueryDto> getNews() {
        LocalDateTime now = LocalDateTime.now();
        return newsRepository.findByCreatedIn(now.minusHours(24), now)
                .stream()
                .map(news -> new NewsQueryDto(news.getTitle(), news.getDescription(), news.getImageUrl(), news.getSource()))
                .toList();
    }

    @Override
    public WeatherQueryDto getWeatherStatus() {
        return weatherRepository.findFirstByOrderByCreatedDesc()
                .map(weather -> new WeatherQueryDto(weather.getCity(), weather.getTemperature(), weather.getWeatherType(), weather.getUrlToIcon(), weather.getHumidity(), weather.getWindSpeed()))
                .orElse(null);
    }
}
