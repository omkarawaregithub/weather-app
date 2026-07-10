package com.jenkins_project.jenkins_project.weather;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

class WeatherServiceTest {

    @Test
    void returnsDemoWeatherWhenApiKeyIsMissing() {
        WeatherService weatherService = new WeatherService("", "https://api.openweathermap.org", "Pune");

        WeatherResponse response = weatherService.getWeather("Pune", null, null);

        assertNotNull(response);
        assertEquals("Pune", response.getCity());
        assertEquals("IN", response.getCountry());
        assertEquals("Clear", response.getCondition());
    }
}
