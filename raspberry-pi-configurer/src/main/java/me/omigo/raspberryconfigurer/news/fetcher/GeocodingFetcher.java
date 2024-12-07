package me.omigo.raspberryconfigurer.news.fetcher;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.omigo.raspberryconfigurer.config.Attributes;
import me.omigo.raspberryconfigurer.news.domain.ConfigRefresher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class GeocodingFetcher {
    private static final String GEOCODING_API_URL = "http://api.openweathermap.org/geo/1.0/direct?q=%s,%s&appid=%s";
    private final RestTemplate restTemplate;
    private final ConfigRefresher configRefresher;

    public GeocodingJson fetchGeocoding(String city) {
        return restTemplate.getForObject(buildUrl(city, "pl"), GeocodingJson[].class)[0];
    }

    private String buildUrl(String city, String country) {
        return String.format(GEOCODING_API_URL, city, country, configRefresher.getAttribute(Attributes.WEATHER_API_KEY));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GeocodingJson {
        private Double lat;
        private Double lon;
    }
}
