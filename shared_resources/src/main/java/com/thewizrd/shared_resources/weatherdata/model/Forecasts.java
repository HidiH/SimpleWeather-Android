package com.thewizrd.shared_resources.weatherdata.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.List;

@Entity(tableName = "forecasts")
public class Forecasts {
    @PrimaryKey
    @NonNull
    private String query;
    @Nullable
    @ColumnInfo(name = "forecastblob")
    private List<Forecast> forecast;
    @Nullable
    @ColumnInfo(name = "txtforecastblob")
    private List<TextForecast> txtForecast;
    @Nullable
    @ColumnInfo(name = "minforecastblob")
    private List<MinutelyForecast> minForecast;
    @Nullable
    @ColumnInfo(name = "aqiforecastblob")
    private List<AirQuality> aqiForecast;

    public Forecasts() {
    }

    @Ignore
    public Forecasts(@NonNull Weather weatherData) {
        this.query = weatherData.getQuery();
        this.forecast = weatherData.getForecast();
        this.txtForecast = weatherData.getTxtForecast();
        this.minForecast = weatherData.getMinForecast();
        this.aqiForecast = weatherData.getAqiForecast();
    }

    @NonNull
    public String getQuery() {
        return query;
    }

    public void setQuery(@NonNull String query) {
        this.query = query;
    }

    @Nullable
    public List<Forecast> getForecast() {
        return forecast;
    }

    public void setForecast(@Nullable List<Forecast> forecast) {
        this.forecast = forecast;
    }

    @Nullable
    public List<TextForecast> getTxtForecast() {
        return txtForecast;
    }

    public void setTxtForecast(@Nullable List<TextForecast> txtForecast) {
        this.txtForecast = txtForecast;
    }

    @Nullable
    public List<MinutelyForecast> getMinForecast() {
        return minForecast;
    }

    public void setMinForecast(@Nullable List<MinutelyForecast> minForecast) {
        this.minForecast = minForecast;
    }

    @Nullable
    public List<AirQuality> getAqiForecast() {
        return aqiForecast;
    }

    public void setAqiForecast(@Nullable List<AirQuality> aqiForecast) {
        this.aqiForecast = aqiForecast;
    }
}
