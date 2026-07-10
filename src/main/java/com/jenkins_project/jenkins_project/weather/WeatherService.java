package com.jenkins_project.jenkins_project.weather;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class WeatherService {

    private static final Duration CACHE_DURATION = Duration.ofMinutes(15);
    private final String apiKey;
    private final String apiBaseUrl;
    private final String defaultCity;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public WeatherService(
            @Value("${openweathermap.api.key:}") String apiKey,
            @Value("${openweathermap.api.base-url:https://api.openweathermap.org}") String apiBaseUrl,
            @Value("${openweathermap.api.default-city:London}") String defaultCity
    ) {
        this.apiKey = apiKey != null ? apiKey.trim() : "";
        this.apiBaseUrl = apiBaseUrl != null ? apiBaseUrl.trim() : "https://api.openweathermap.org";
        this.defaultCity = defaultCity != null && !defaultCity.isBlank() ? defaultCity.trim() : "London";
        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    public WeatherResponse getWeather(String city, Double lat, Double lon) {
        if ((city == null || city.isBlank()) && (lat == null || lon == null)) {
            city = defaultCity;
        }

        if (apiKey == null || apiKey.isBlank()) {
            return getDemoWeather(city, lat, lon);
        }

        String cacheKey = buildCacheKey(city, lat, lon);
        CacheEntry cached = cache.get(cacheKey);
        if (cached != null && !cached.isExpired()) {
            return cached.weather().withCached(false);
        }

        try {
            WeatherResponse response = fetchWeather(city, lat, lon);
            cache.put(cacheKey, new CacheEntry(response, Instant.now(Clock.systemUTC())));
            return response;
        } catch (ResponseStatusException e) {
            System.err.println("WeatherService: ResponseStatusException while fetching weather: " + e.getMessage());
            e.printStackTrace();
            if (cached != null) {
                return cached.weather().withCached(true);
            }
            throw e;
        } catch (IOException | InterruptedException e) {
            System.err.println("WeatherService: Exception while fetching weather: " + e.getMessage());
            e.printStackTrace();
            if (cached != null) {
                return cached.weather().withCached(true);
            }
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Unable to fetch weather data. Check your network connection.", e);
        }
    }

    private String buildCacheKey(String city, Double lat, Double lon) {
        if (lat != null && lon != null) {
            return String.format(Locale.ROOT, "%.5f,%.5f", lat, lon);
        }
        return city.trim().toLowerCase(Locale.ROOT);
    }

    private WeatherResponse getDemoWeather(String city, Double lat, Double lon) {
        String demoCity = city != null ? city.trim() : defaultCity;
        String demoCountry = demoCity.equals(defaultCity) ? "IN" : "US";
        
        List<WeatherResponse.HourlyForecast> hourly = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            String time = String.format("%02d:00", i);
            double temp = 20 + (i % 8) * 2;
            String icon = i < 6 || i > 18 ? "01n" : "01d";
            hourly.add(new WeatherResponse.HourlyForecast(time, temp, icon, 10, "Clear"));
        }
        
        List<WeatherResponse.DailyForecast> daily = new ArrayList<>();
        String[] days = {"Today", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        for (int i = 0; i < 7; i++) {
            daily.add(new WeatherResponse.DailyForecast(days[i], 18 + i, 28 + i, "01d", 5, "Sunny"));
        }
        
        return new WeatherResponse(
                demoCity,
                demoCountry,
                "Clear",
                "Clear sky",
                "01d",
                24.5,
                22.0,
                65,
                1013,
                10,
                8.5,
                5.5,
                "6:30 AM",
                "6:45 PM",
                "Today • 2:30 PM",
                "UTC",
                false,
                true,
                hourly,
                daily
        );
    }

    private WeatherResponse fetchWeather(String city, Double lat, Double lon) throws IOException, InterruptedException {
        String locationName = city != null ? city.trim() : defaultCity;
        String country = "";
        // Use OpenWeather geocoding and One Call API
        if (lat == null || lon == null) {
            JsonNode geoNode = callApi(String.format("http://api.openweathermap.org/geo/1.0/direct?q=%s&limit=1&appid=%s",
                    URLEncoder.encode(locationName, StandardCharsets.UTF_8), apiKey));
            if (!geoNode.isArray() || geoNode.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "City not found: " + locationName);
            }
            JsonNode first = geoNode.get(0);
            lat = first.path("lat").asDouble();
            lon = first.path("lon").asDouble();
            locationName = first.path("name").asText(locationName);
            country = first.path("country").asText("");
            String state = first.path("state").asText();
            if (!state.isBlank()) locationName += ", " + state;
        } else {
            JsonNode reverseGeo = callApi(String.format("http://api.openweathermap.org/geo/1.0/reverse?lat=%s&lon=%s&limit=1&appid=%s",
                    lat, lon, apiKey));
            if (reverseGeo.isArray() && !reverseGeo.isEmpty()) {
                JsonNode first = reverseGeo.get(0);
                locationName = first.path("name").asText(locationName);
                country = first.path("country").asText("");
                String state = first.path("state").asText();
                if (!state.isBlank()) locationName += ", " + state;
            }
        }

        // Use free OpenWeather endpoints: current weather + 5-day/3-hour forecast
        JsonNode currentNode = callApi(String.format("https://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric&appid=%s",
            lat, lon, apiKey));
        JsonNode forecastNode = callApi(String.format("https://api.openweathermap.org/data/2.5/forecast?lat=%s&lon=%s&units=metric&appid=%s",
            lat, lon, apiKey));
        return mapWeatherFromOpenWeatherV2(locationName, country, currentNode, forecastNode);
    }

    private WeatherResponse mapWeatherFromOpenWeather(String city, String country, JsonNode node) {
        JsonNode current = node.path("current");
        JsonNode hourly = node.path("hourly");
        JsonNode daily = node.path("daily");
        String timezone = node.path("timezone").asText("UTC");
        int tzOffset = node.path("timezone_offset").asInt(0);

        double temperature = round(current.path("temp").asDouble(0.0));
        double windSpeed = round(current.path("wind_speed").asDouble(0.0));
        JsonNode curWeather = current.path("weather");
        String iconCode = curWeather.isArray() && curWeather.size() > 0 ? curWeather.get(0).path("icon").asText("01d") : "01d";
        String condition = curWeather.isArray() && curWeather.size() > 0 ? curWeather.get(0).path("main").asText("Clear") : "Clear";
        String description = curWeather.isArray() && curWeather.size() > 0 ? curWeather.get(0).path("description").asText("") : condition;
        boolean night = iconCode.endsWith("n");

        long currentDt = current.path("dt").asLong(0L);
        String localTime = currentDt > 0 ? formatInstant(currentDt, tzOffset, "MMM d • h:mm a") : "Current conditions";

        double feelsLike = round(current.path("feels_like").asDouble(temperature));
        int humidity = current.path("humidity").asInt(0);
        int pressure = current.path("pressure").asInt(0);
        int visibility = (int) Math.round(current.path("visibility").asDouble(0.0) / 1000.0);
        double uvIndex = round(current.path("uvi").asDouble(0.0));

        List<WeatherResponse.HourlyForecast> hourlyList = new ArrayList<>();
        for (int i = 0; i < Math.min(24, hourly.size()); i++) {
            JsonNode h = hourly.get(i);
            long dt = h.path("dt").asLong(0L);
            String time = dt > 0 ? formatInstant(dt, tzOffset, "h a") : "";
            double temp = round(h.path("temp").asDouble(0.0));
            JsonNode hw = h.path("weather");
            String icon = hw.isArray() && hw.size() > 0 ? hw.get(0).path("icon").asText("01d") : "01d";
            int pop = (int) Math.round(h.path("pop").asDouble(0.0) * 100.0);
            String hourDesc = hw.isArray() && hw.size() > 0 ? hw.get(0).path("main").asText("") : "";
            hourlyList.add(new WeatherResponse.HourlyForecast(time, temp, icon, pop, hourDesc));
        }

        List<WeatherResponse.DailyForecast> dailyList = new ArrayList<>();
        for (int i = 0; i < Math.min(7, daily.size()); i++) {
            JsonNode d = daily.get(i);
            long dt = d.path("dt").asLong(0L);
            String dateIso = dt > 0 ? Instant.ofEpochSecond(dt).atZone(ZoneId.of(timezone)).toLocalDate().toString() : "";
            String dayLabel = i == 0 ? "Today" : formatDailyLabel(dateIso, false);
            double minTemp = round(d.path("temp").path("min").asDouble(0.0));
            double maxTemp = round(d.path("temp").path("max").asDouble(0.0));
            JsonNode dw = d.path("weather");
            String icon = dw.isArray() && dw.size() > 0 ? dw.get(0).path("icon").asText("01d") : "01d";
            int pop = (int) Math.round(d.path("pop").asDouble(0.0) * 100.0);
            String dayDesc = dw.isArray() && dw.size() > 0 ? dw.get(0).path("main").asText("") : "";
            dailyList.add(new WeatherResponse.DailyForecast(dayLabel, minTemp, maxTemp, icon, pop, dayDesc));
        }

        long sunriseEpoch = current.path("sunrise").asLong(0L);
        long sunsetEpoch = current.path("sunset").asLong(0L);
        String sunrise = sunriseEpoch > 0 ? formatInstant(sunriseEpoch, tzOffset, "h:mm a") : "--";
        String sunset = sunsetEpoch > 0 ? formatInstant(sunsetEpoch, tzOffset, "h:mm a") : "--";

        return new WeatherResponse(
                city,
                country,
                condition,
                description,
                iconCode,
                temperature,
                feelsLike,
                humidity,
                pressure,
                visibility,
                windSpeed,
                uvIndex,
                sunrise,
                sunset,
                localTime,
                timezone,
                night,
                false,
                hourlyList,
                dailyList
        );
    }

    private WeatherResponse mapWeatherFromOpenWeatherV2(String city, String country, JsonNode currentNode, JsonNode forecastNode) {
        String timezone = "UTC";
        int tzOffset = currentNode.path("timezone").asInt(0);

        JsonNode curWeather = currentNode.path("weather");
        String iconCode = curWeather.isArray() && curWeather.size() > 0 ? curWeather.get(0).path("icon").asText("01d") : "01d";
        String condition = curWeather.isArray() && curWeather.size() > 0 ? curWeather.get(0).path("main").asText("Clear") : "Clear";
        String description = curWeather.isArray() && curWeather.size() > 0 ? curWeather.get(0).path("description").asText("") : condition;
        boolean night = iconCode.endsWith("n");

        double temperature = round(currentNode.path("main").path("temp").asDouble(0.0));
        double feelsLike = round(currentNode.path("main").path("feels_like").asDouble(temperature));
        int humidity = currentNode.path("main").path("humidity").asInt(0);
        int pressure = currentNode.path("main").path("pressure").asInt(0);
        int visibility = (int) Math.round(currentNode.path("visibility").asDouble(0.0) / 1000.0);
        double windSpeed = round(currentNode.path("wind").path("speed").asDouble(0.0) * 3.6);
        double uvIndex = 0.0; // not available in free endpoints

        long currentDt = currentNode.path("dt").asLong(0L);
        String localTime = currentDt > 0 ? formatInstant(currentDt, tzOffset, "MMM d • h:mm a") : "Current conditions";

        List<WeatherResponse.HourlyForecast> hourlyList = new ArrayList<>();
        JsonNode list = forecastNode.path("list");
        for (int i = 0; i < Math.min(24, list.size()); i++) {
            JsonNode h = list.get(i);
            long dt = h.path("dt").asLong(0L);
            String time = dt > 0 ? formatInstant(dt, tzOffset, "h a") : "";
            double temp = round(h.path("main").path("temp").asDouble(0.0));
            JsonNode hw = h.path("weather");
            String icon = hw.isArray() && hw.size() > 0 ? hw.get(0).path("icon").asText("01d") : "01d";
            int pop = (int) Math.round(h.path("pop").asDouble(0.0) * 100.0);
            String hourDesc = hw.isArray() && hw.size() > 0 ? hw.get(0).path("main").asText("") : "";
            hourlyList.add(new WeatherResponse.HourlyForecast(time, temp, icon, pop, hourDesc));
        }

        // Aggregate daily min/max from 3-hourly forecast
        java.util.Map<String, double[]> dailyMap = new java.util.LinkedHashMap<>();
        java.util.Map<String, Integer> dailyPop = new java.util.HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            JsonNode h = list.get(i);
            long dt = h.path("dt").asLong(0L);
            String date = Instant.ofEpochSecond(dt).atZone(ZoneOffset.ofTotalSeconds(tzOffset)).toLocalDate().toString();
            double temp = h.path("main").path("temp").asDouble(Double.NaN);
            int pop = (int) Math.round(h.path("pop").asDouble(0.0) * 100.0);
            double[] mm = dailyMap.getOrDefault(date, new double[]{Double.NaN, Double.NaN});
            if (Double.isNaN(mm[0]) || temp < mm[0]) mm[0] = temp;
            if (Double.isNaN(mm[1]) || temp > mm[1]) mm[1] = temp;
            dailyMap.put(date, mm);
            dailyPop.put(date, Math.max(dailyPop.getOrDefault(date, 0), pop));
        }

        List<WeatherResponse.DailyForecast> dailyList = new ArrayList<>();
        int i = 0;
        for (java.util.Map.Entry<String, double[]> e : dailyMap.entrySet()) {
            if (i++ >= 7) break;
            String date = e.getKey();
            double minTemp = round(e.getValue()[0]);
            double maxTemp = round(e.getValue()[1]);
            int pop = dailyPop.getOrDefault(date, 0);
            String dayLabel = i == 1 ? "Today" : formatDailyLabel(date, false);
            String icon = "01d";
            String dayDesc = "";
            dailyList.add(new WeatherResponse.DailyForecast(dayLabel, minTemp, maxTemp, icon, pop, dayDesc));
        }

        long sunriseEpoch = currentNode.path("sys").path("sunrise").asLong(0L);
        long sunsetEpoch = currentNode.path("sys").path("sunset").asLong(0L);
        String sunrise = sunriseEpoch > 0 ? formatInstant(sunriseEpoch, tzOffset, "h:mm a") : "--";
        String sunset = sunsetEpoch > 0 ? formatInstant(sunsetEpoch, tzOffset, "h:mm a") : "--";

        return new WeatherResponse(
                city,
                country,
                condition,
                description,
                iconCode,
                temperature,
                feelsLike,
                humidity,
                pressure,
                visibility,
                windSpeed,
                uvIndex,
                sunrise,
                sunset,
                localTime,
                timezone,
                night,
                false,
                hourlyList,
                dailyList
        );
    }

    private JsonNode callApi(String uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .timeout(Duration.ofSeconds(15))
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Weather service responded with status " + response.statusCode());
        }
        return objectMapper.readTree(response.body());
    }

    private WeatherResponse mapWeather(String city, String country, JsonNode weatherNode) {
        JsonNode current = weatherNode.path("current_weather");
        JsonNode hourly = weatherNode.path("hourly");
        JsonNode daily = weatherNode.path("daily");
        String timezone = weatherNode.path("timezone").asText("UTC");

        double temperature = round(current.path("temperature").asDouble(0.0));
        double windSpeed = round(current.path("windspeed").asDouble(0.0));
        String iconCode = mapWeatherCodeToIcon(current.path("weathercode").asInt(0), current.path("is_day").asInt(1) == 1);
        String condition = mapWeatherCodeToCondition(current.path("weathercode").asInt(0));
        String description = condition;
        boolean night = current.path("is_day").asInt(1) == 0;

        String currentTime = current.path("time").asText("");
        String localTime = currentTime.isBlank() ? "Current conditions" : currentTime;
        String timezoneSuffix = timezone != null ? timezone : "UTC";

        int hourIndex = 0;
        String hourString = hourly.path("time").isArray() && hourly.path("time").size() > 0 ? hourly.path("time").get(0).asText() : currentTime;
        for (int i = 0; i < hourly.path("time").size(); i++) {
            if (hourly.path("time").get(i).asText().equals(currentTime)) {
                hourIndex = i;
                break;
            }
        }

        double feelsLike = round(hourly.path("apparent_temperature").path(hourIndex).asDouble(temperature));
        int humidity = hourly.path("relativehumidity_2m").path(hourIndex).asInt(0);
        int pressure = (int) Math.round(hourly.path("pressure_msl").path(hourIndex).asDouble(0.0));
        int visibility = (int) Math.round(hourly.path("visibility").path(hourIndex).asDouble(0.0) / 1000.0);
        double uvIndex = round(hourly.path("uv_index").path(hourIndex).asDouble(0.0));

        List<WeatherResponse.HourlyForecast> hourlyList = new ArrayList<>();
        JsonNode hourlyTimes = hourly.path("time");
        JsonNode hourlyTemps = hourly.path("temperature_2m");
        JsonNode hourlyIcons = hourly.path("weathercode");
        JsonNode hourlyPop = hourly.path("precipitation_probability");
        for (int i = 0; i < Math.min(24, hourlyTimes.size()); i++) {
            String time = formatHour(hourlyTimes.get(i).asText(""));
            double temp = round(hourlyTemps.get(i).asDouble(0.0));
            int code = hourlyIcons.get(i).asInt(0);
            String icon = mapWeatherCodeToIcon(code, isDayHour(hourlyTimes.get(i).asText(""), timezone));
            int pop = hourlyPop.get(i).asInt(0);
            String hourDesc = mapWeatherCodeToCondition(code);
            hourlyList.add(new WeatherResponse.HourlyForecast(time, temp, icon, pop, hourDesc));
        }

        List<WeatherResponse.DailyForecast> dailyList = new ArrayList<>();
        JsonNode dailyDates = daily.path("time");
        JsonNode dailyMin = daily.path("temperature_2m_min");
        JsonNode dailyMax = daily.path("temperature_2m_max");
        JsonNode dailyCodes = daily.path("weathercode");
        JsonNode dailyPopMax = daily.path("precipitation_probability_max");
        for (int i = 0; i < Math.min(7, dailyDates.size()); i++) {
            String dayLabel = formatDailyLabel(dailyDates.get(i).asText(""), i == 0);
            double minTemp = round(dailyMin.get(i).asDouble(0.0));
            double maxTemp = round(dailyMax.get(i).asDouble(0.0));
            int code = dailyCodes.get(i).asInt(0);
            String icon = mapWeatherCodeToIcon(code, true);
            int pop = dailyPopMax.get(i).asInt(0);
            String dayDesc = mapWeatherCodeToCondition(code);
            dailyList.add(new WeatherResponse.DailyForecast(dayLabel, minTemp, maxTemp, icon, pop, dayDesc));
        }

        String sunrise = daily.path("sunrise").isArray() && daily.path("sunrise").size() > 0 ? daily.path("sunrise").get(0).asText("") : "--";
        String sunset = daily.path("sunset").isArray() && daily.path("sunset").size() > 0 ? daily.path("sunset").get(0).asText("") : "--";

        return new WeatherResponse(
                city,
                country,
                condition,
                description,
                iconCode,
                temperature,
                feelsLike,
                humidity,
                pressure,
                visibility,
                windSpeed,
                uvIndex,
                sunrise,
                sunset,
                localTime,
                timezoneSuffix,
                night,
                false,
                hourlyList,
                dailyList
        );
    }

    private String mapWeatherCodeToCondition(int code) {
        if (code == 0) return "Clear";
        if (code == 1 || code == 2) return "Partly cloudy";
        if (code == 3) return "Cloudy";
        if (code == 45 || code == 48) return "Fog";
        if (code >= 51 && code <= 67) return "Drizzle";
        if (code >= 71 && code <= 77) return "Snow";
        if (code >= 80 && code <= 82) return "Rain";
        if (code >= 95) return "Thunderstorm";
        return "Clear";
    }

    private String mapWeatherCodeToIcon(int code, boolean isDay) {
        String suffix = isDay ? "d" : "n";
        if (code == 0) return "01" + suffix;
        if (code == 1 || code == 2) return "02" + suffix;
        if (code == 3) return "03" + suffix;
        if (code == 45 || code == 48) return "50" + suffix;
        if (code >= 51 && code <= 67) return "09" + suffix;
        if (code >= 71 && code <= 77) return "13" + suffix;
        if (code >= 80 && code <= 82) return "10" + suffix;
        if (code >= 95) return "11" + suffix;
        return "01" + suffix;
    }

    private boolean isDayHour(String timeIso, String timezone) {
        try {
            return LocalDate.parse(timeIso.substring(0, 10)).atStartOfDay(ZoneId.of(timezone)).getHour() >= 6;
        } catch (Exception e) {
            return true;
        }
    }

    private String formatHour(String timeIso) {
        if (timeIso == null || timeIso.isBlank()) {
            return "";
        }
        try {
            String hour = timeIso.substring(11, 13);
            return Integer.parseInt(hour) == 0 ? "12 AM" : Integer.parseInt(hour) < 12 ? hour + " AM" : (Integer.parseInt(hour) == 12 ? "12 PM" : (Integer.parseInt(hour) - 12) + " PM");
        } catch (Exception e) {
            return timeIso;
        }
    }

    private String formatDailyLabel(String dateIso, boolean isToday) {
        if (isToday) {
            return "Today";
        }
        try {
            LocalDate date = LocalDate.parse(dateIso);
            return date.getDayOfWeek().name().substring(0, 3);
        } catch (Exception e) {
            return dateIso;
        }
    }

    private String formatInstant(long epochSeconds, int offsetSeconds, String pattern) {
        return Instant.ofEpochSecond(epochSeconds)
                .atOffset(ZoneOffset.ofTotalSeconds(offsetSeconds))
                .format(DateTimeFormatter.ofPattern(pattern, Locale.ENGLISH));
    }

    private double round(double value) {
        return Math.round(value * 10.0) / 10.0;
    }

    private record CacheEntry(WeatherResponse weather, Instant timestamp) {
        boolean isExpired() {
            return Instant.now(Clock.systemUTC()).isAfter(timestamp.plus(CACHE_DURATION));
        }
    }
}
