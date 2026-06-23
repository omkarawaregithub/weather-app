package com.jenkins_project.jenkins_project.weather;

import java.util.List;

public class WeatherResponse {
    private final String city;
    private final String country;
    private final String condition;
    private final String description;
    private final String icon;
    private final double temperatureCelsius;
    private final double feelsLikeCelsius;
    private final int humidity;
    private final int pressure;
    private final int visibilityKm;
    private final double windSpeedKph;
    private final double uvIndex;
    private final String sunrise;
    private final String sunset;
    private final String localTime;
    private final String timezone;
    private final boolean night;
    private final boolean cached;
    private final List<HourlyForecast> hourlyForecast;
    private final List<DailyForecast> dailyForecast;

    public WeatherResponse(
            String city,
            String country,
            String condition,
            String description,
            String icon,
            double temperatureCelsius,
            double feelsLikeCelsius,
            int humidity,
            int pressure,
            int visibilityKm,
            double windSpeedKph,
            double uvIndex,
            String sunrise,
            String sunset,
            String localTime,
            String timezone,
            boolean night,
            boolean cached,
            List<HourlyForecast> hourlyForecast,
            List<DailyForecast> dailyForecast
    ) {
        this.city = city;
        this.country = country;
        this.condition = condition;
        this.description = description;
        this.icon = icon;
        this.temperatureCelsius = temperatureCelsius;
        this.feelsLikeCelsius = feelsLikeCelsius;
        this.humidity = humidity;
        this.pressure = pressure;
        this.visibilityKm = visibilityKm;
        this.windSpeedKph = windSpeedKph;
        this.uvIndex = uvIndex;
        this.sunrise = sunrise;
        this.sunset = sunset;
        this.localTime = localTime;
        this.timezone = timezone;
        this.night = night;
        this.cached = cached;
        this.hourlyForecast = hourlyForecast;
        this.dailyForecast = dailyForecast;
    }

    public WeatherResponse withCached(boolean cached) {
        return new WeatherResponse(city, country, condition, description, icon, temperatureCelsius, feelsLikeCelsius,
                humidity, pressure, visibilityKm, windSpeedKph, uvIndex, sunrise, sunset, localTime, timezone, night,
                cached, hourlyForecast, dailyForecast);
    }

    public String getCity() { return city; }
    public String getCountry() { return country; }
    public String getCondition() { return condition; }
    public String getDescription() { return description; }
    public String getIcon() { return icon; }
    public double getTemperatureCelsius() { return temperatureCelsius; }
    public double getFeelsLikeCelsius() { return feelsLikeCelsius; }
    public int getHumidity() { return humidity; }
    public int getPressure() { return pressure; }
    public int getVisibilityKm() { return visibilityKm; }
    public double getWindSpeedKph() { return windSpeedKph; }
    public double getUvIndex() { return uvIndex; }
    public String getSunrise() { return sunrise; }
    public String getSunset() { return sunset; }
    public String getLocalTime() { return localTime; }
    public String getTimezone() { return timezone; }
    public boolean isNight() { return night; }
    public boolean isCached() { return cached; }
    public List<HourlyForecast> getHourlyForecast() { return hourlyForecast; }
    public List<DailyForecast> getDailyForecast() { return dailyForecast; }

    public static class HourlyForecast {
        private final String time;
        private final double temperatureCelsius;
        private final String icon;
        private final int precipitationChance;
        private final String condition;

        public HourlyForecast(String time, double temperatureCelsius, String icon, int precipitationChance, String condition) {
            this.time = time;
            this.temperatureCelsius = temperatureCelsius;
            this.icon = icon;
            this.precipitationChance = precipitationChance;
            this.condition = condition;
        }

        public String getTime() { return time; }
        public double getTemperatureCelsius() { return temperatureCelsius; }
        public String getIcon() { return icon; }
        public int getPrecipitationChance() { return precipitationChance; }
        public String getCondition() { return condition; }
    }

    public static class DailyForecast {
        private final String day;
        private final double minTemperatureCelsius;
        private final double maxTemperatureCelsius;
        private final String icon;
        private final int precipitationChance;
        private final String condition;

        public DailyForecast(String day, double minTemperatureCelsius, double maxTemperatureCelsius, String icon, int precipitationChance, String condition) {
            this.day = day;
            this.minTemperatureCelsius = minTemperatureCelsius;
            this.maxTemperatureCelsius = maxTemperatureCelsius;
            this.icon = icon;
            this.precipitationChance = precipitationChance;
            this.condition = condition;
        }

        public String getDay() { return day; }
        public double getMinTemperatureCelsius() { return minTemperatureCelsius; }
        public double getMaxTemperatureCelsius() { return maxTemperatureCelsius; }
        public String getIcon() { return icon; }
        public int getPrecipitationChance() { return precipitationChance; }
        public String getCondition() { return condition; }
    }
}
