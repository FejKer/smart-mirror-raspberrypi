package me.omigo.raspberryconfigurer.news.fetcher;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import me.omigo.raspberryconfigurer.config.Attributes;
import me.omigo.raspberryconfigurer.news.domain.ConfigRefresher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class WeatherFetcher {

    private static final String WEATHER_API_URL = "https://api.openweathermap.org/data/2.5/weather?lat=%f&lon=%f&appid=%s&lang=%s";
    private final RestTemplate restTemplate;

    private final GeocodingFetcher geocodingFetcher;
    private final ConfigRefresher configRefresher;

    public WeatherJson fetchWeather() {
        GeocodingFetcher.GeocodingJson geocodingJson = geocodingFetcher.fetchGeocoding(configRefresher.getAttribute(Attributes.LOCATION));
        return restTemplate.getForObject(buildUrl(geocodingJson), WeatherJson.class);
    }

    private String buildUrl(GeocodingFetcher.GeocodingJson geocodingJson) {
        return String.format(WEATHER_API_URL, geocodingJson.getLat(), geocodingJson.getLon(), configRefresher.getAttribute(Attributes.WEATHER_API_KEY), configRefresher.getAttribute(Attributes.NEWS_LANGUAGE));
    }

    public static class Clouds{
        public int all;
    }

    public static class Coord{
        public double lon;
        public double lat;
    }

    public static class Main{
        public double temp;
        public double feels_like;
        public double temp_min;
        public double temp_max;
        public int pressure;
        public int humidity;
        public int sea_level;
        public int grnd_level;
    }

    public static class WeatherJson{
        public Coord coord;
        public ArrayList<Weather> weather;
        public String base;
        public Main main;
        public int visibility;
        public Wind wind;
        public Clouds clouds;
        public int dt;
        public Sys sys;
        public int timezone;
        public int id;
        public String name;
        public int cod;
    }

    public static class Sys{
        public int type;
        public int id;
        public String country;
        public int sunrise;
        public int sunset;
    }

    public static class Weather{
        public int id;
        public String main;
        public String description;
        public String icon;
    }

    public static class Wind{
        public double speed;
        public int deg;
        public double gust;
    }


}
