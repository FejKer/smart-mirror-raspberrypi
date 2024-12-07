package me.omigo.raspberryconfigurer.news.command;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.omigo.raspberryconfigurer.config.Attributes;
import me.omigo.raspberryconfigurer.config.domain.ConfigService;
import me.omigo.raspberryconfigurer.news.NewsRepository;
import me.omigo.raspberryconfigurer.news.WeatherRepository;
import me.omigo.raspberryconfigurer.news.domain.ConfigRefresher;
import me.omigo.raspberryconfigurer.news.domain.News;
import me.omigo.raspberryconfigurer.news.domain.Weather;
import me.omigo.raspberryconfigurer.news.fetcher.NewsFetcher;
import me.omigo.raspberryconfigurer.news.fetcher.WeatherFetcher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class NewsWeatherUpdater {
    private final WeatherFetcher weatherFetcher;
    private final NewsFetcher newsFetcher;
    private final WeatherRepository weatherRepository;
    private final NewsRepository newsRepository;
    private final ConfigRefresher configRefresher;

    @Scheduled(timeUnit = TimeUnit.MINUTES, fixedRate = 15)
    void update() {
        log.info("Updating weather");
        WeatherFetcher.WeatherJson weatherJson = weatherFetcher.fetchWeather();
        saveWeather(weatherJson);
    }

    @Scheduled(timeUnit = TimeUnit.MINUTES, fixedRate = 180)
    void updateNews() {
        List<NewsFetcher.NewsJson> newsJson = newsFetcher.fetchNews();
        for (var news : newsJson) {
            saveNews(news);
        }
    }

    private void saveNews(NewsFetcher.NewsJson newsJson) {
        newsJson.articles.forEach(article -> {
            News news = new News();
            news.setTitle(article.title);
            news.setDescription(article.description);
            news.setCreated(LocalDateTime.now());
            news.setSource(article.source.name);
            news.setImageUrl(article.urlToImage);
            if (newsRepository.existsByTitleIgnoreCaseAndDescriptionIgnoreCase(news.getTitle(), news.getDescription())) {
                return;
            }
            newsRepository.save(news);
        });
    }

    private void saveWeather(WeatherFetcher.WeatherJson weatherJson) {
        Weather weather = new Weather();
        weather.setWeatherType(weatherJson.weather.getFirst().description);
        weather.setCreated(LocalDateTime.now());
        weather.setCity(configRefresher.getAttribute(Attributes.LOCATION));
        weather.setTemperature(convertFromKelvinToCelcius(weatherJson.main.temp));
        weather.setUrlToIcon(String.format("https://openweathermap.org/img/wn/%s@2x.png", weatherJson.weather.getFirst().icon));
        weather.setHumidity((double) weatherJson.main.humidity);
        weather.setWindSpeed(weatherJson.wind.speed);
        log.info("Saving {}", weather);
        weatherRepository.save(weather);
    }

    private Double convertFromKelvinToCelcius(double temp) {
        return temp - 273.15;
    }

    @EventListener
    public void handleMyCustomEvent(ConfigService.NewsWeatherReloadEvent event) {
        log.info("Received news reload event");
        try {
            refresh();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Async
    public void refresh() throws InterruptedException {
        try {
            newsRepository.deleteAll();
            update();
            updateNews();
        } catch (Exception e) {
            log.error("Error while refreshing {}", e.getMessage());
            Thread.sleep(Duration.of(1, TimeUnit.MINUTES.toChronoUnit()));
            refresh();
        }
    }
}
