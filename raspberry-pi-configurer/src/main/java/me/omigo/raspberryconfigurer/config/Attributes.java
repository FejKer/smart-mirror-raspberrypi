package me.omigo.raspberryconfigurer.config;

import java.util.Arrays;
import java.util.List;

public enum Attributes {
    NEWS_LANGUAGE("news-language"),
    NEWS_CATEGORIES("news-categories"),
    WEATHER_API_KEY("weather-api-key"),
    NEWS_API_KEY("news-api-key"),
    LOCATION("location"),
    WIFI_SSID("wifi-ssid"),
    WIFI_PASSWORD("wifi-password");

    private final String key;

    Attributes(String key) {
        this.key = key;
    }

    public String getKey() {
        return this.key;
    }

    public static List<String> getKeysList() {
        return Arrays.stream(Attributes.values()).map(Attributes::getKey).toList();
    }
}
