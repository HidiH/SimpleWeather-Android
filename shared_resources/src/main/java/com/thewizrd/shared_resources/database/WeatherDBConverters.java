package com.thewizrd.shared_resources.database;

import android.annotation.SuppressLint;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.thewizrd.shared_resources.utils.DateTimeUtils;
import com.thewizrd.shared_resources.utils.JSONParser;
import com.thewizrd.shared_resources.utils.Logger;
import com.thewizrd.shared_resources.weatherdata.model.AirQuality;
import com.thewizrd.shared_resources.weatherdata.model.Astronomy;
import com.thewizrd.shared_resources.weatherdata.model.Atmosphere;
import com.thewizrd.shared_resources.weatherdata.model.Condition;
import com.thewizrd.shared_resources.weatherdata.model.Forecast;
import com.thewizrd.shared_resources.weatherdata.model.HourlyForecast;
import com.thewizrd.shared_resources.weatherdata.model.Location;
import com.thewizrd.shared_resources.weatherdata.model.MinutelyForecast;
import com.thewizrd.shared_resources.weatherdata.model.Precipitation;
import com.thewizrd.shared_resources.weatherdata.model.TextForecast;
import com.thewizrd.shared_resources.weatherdata.model.WeatherAlert;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import okio.Buffer;

public class WeatherDBConverters {
    private static final DateTimeFormatter zDTF = DateTimeUtils.getZonedDateTimeFormatter();
    private static final DateTimeFormatter lDTF = DateTimeFormatter.ISO_INSTANT;

    @TypeConverter
    public static @Nullable Location locationFromJson(@Nullable String value) {
        return JSONParser.deserializer(value, Location.class);
    }

    @TypeConverter
    public static @Nullable String locationToJson(@Nullable Location value) {
        return JSONParser.serializer(value, Location.class);
    }

    @TypeConverter
    public static @Nullable ZonedDateTime zonedDateTimeFromString(@Nullable String value) {
        return value == null ? null : ZonedDateTime.parse(value, zDTF);
    }

    @TypeConverter
    public static @Nullable String zonedDateTimetoString(@Nullable ZonedDateTime value) {
        return value == null ? null : value.format(zDTF);
    }

    @TypeConverter
    public static @Nullable LocalDateTime localDateTimeFromString(@Nullable String value) {
        return value == null ? null : LocalDateTime.ofInstant(Instant.from(lDTF.parse(value)), ZoneOffset.UTC);
    }

    @TypeConverter
    public static @Nullable String localDateTimetoString(@Nullable LocalDateTime value) {
        return value == null ? null : value.toInstant(ZoneOffset.UTC).toString();
    }

    @TypeConverter
    public static @Nullable List<Forecast> forecastArrfromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            JsonReader reader = JsonReader.of(new Buffer().writeUtf8(value));
            List<Forecast> result = new ArrayList<>(10);

            try {
                reader.beginArray();

                while (reader.hasNext()) {
                    Forecast obj = new Forecast();
                    obj.fromJson(reader);
                    result.add(obj);
                }

                reader.endArray();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error parsing JSON");
            }

            return result;
        }
    }

    @TypeConverter
    public static @Nullable String forecastArrtoJson(@Nullable List<Forecast> value) {
        if (value == null)
            return null;
        else {
            Buffer buffer = new Buffer();
            JsonWriter writer = JsonWriter.of(buffer);
            writer.setSerializeNulls(true);

            try {
                writer.beginArray();

                for (Forecast forecast : value) {
                    forecast.toJson(writer);
                }

                writer.endArray();
                writer.close();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error writing JSON");
            }

            return buffer.readUtf8();
        }
    }

    @TypeConverter
    public static @Nullable HourlyForecast hrforecastFromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            HourlyForecast obj = new HourlyForecast();
            obj.fromJson(JsonReader.of(new Buffer().writeUtf8(value)));
            return obj;
        }
    }

    @TypeConverter
    public static @Nullable String hrforecastToJson(@Nullable HourlyForecast value) {
        return JSONParser.serializer(value, HourlyForecast.class);
    }

    @TypeConverter
    public static @Nullable List<HourlyForecast> hrforecastArrfromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            JsonReader reader = JsonReader.of(new Buffer().writeUtf8(value));
            List<HourlyForecast> result = new ArrayList<>(90);

            try {
                reader.beginArray();

                while (reader.hasNext()) {
                    HourlyForecast obj = new HourlyForecast();
                    obj.fromJson(reader);
                    result.add(obj);
                }

                reader.endArray();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error parsing JSON");
            }

            return result;
        }
    }

    @TypeConverter
    public static @Nullable String hrforecastArrtoJson(@Nullable List<HourlyForecast> value) {
        if (value == null)
            return null;
        else {
            Buffer buffer = new Buffer();
            JsonWriter writer = JsonWriter.of(buffer);
            writer.setSerializeNulls(true);

            try {
                writer.beginArray();

                for (HourlyForecast forecast : value) {
                    forecast.toJson(writer);
                }

                writer.endArray();
                writer.close();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error writing JSON");
            }

            return buffer.readUtf8();
        }
    }

    @TypeConverter
    public static @Nullable List<TextForecast> txtforecastArrfromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            JsonReader reader = JsonReader.of(new Buffer().writeUtf8(value));
            ArrayList<TextForecast> result = new ArrayList<>(20);

            try {
                reader.beginArray();

                while (reader.hasNext()) {
                    TextForecast obj = new TextForecast();
                    obj.fromJson(reader);
                    result.add(obj);
                }

                reader.endArray();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error parsing JSON");
            }

            return result;
        }
    }

    @TypeConverter
    public static @Nullable String txtforecastArrtoJson(@Nullable List<TextForecast> value) {
        if (value == null)
            return null;
        else {
            Buffer buffer = new Buffer();
            JsonWriter writer = JsonWriter.of(buffer);
            writer.setSerializeNulls(true);

            try {
                writer.beginArray();

                for (TextForecast forecast : value) {
                    forecast.toJson(writer);
                }

                writer.endArray();
                writer.close();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error writing JSON");
            }

            return buffer.readUtf8();
        }
    }

    @TypeConverter
    public static @Nullable MinutelyForecast minforecastFromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            MinutelyForecast obj = new MinutelyForecast();
            obj.fromJson(JsonReader.of(new Buffer().writeUtf8(value)));
            return obj;
        }
    }

    @TypeConverter
    public static @Nullable String minforecastToJson(@Nullable MinutelyForecast value) {
        return JSONParser.serializer(value, MinutelyForecast.class);
    }

    @TypeConverter
    public static @Nullable List<MinutelyForecast> minforecastArrfromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            JsonReader reader = JsonReader.of(new Buffer().writeUtf8(value));
            List<MinutelyForecast> result = new ArrayList<>(90);

            try {
                reader.beginArray();

                while (reader.hasNext()) {
                    MinutelyForecast obj = new MinutelyForecast();
                    obj.fromJson(reader);
                    result.add(obj);
                }

                reader.endArray();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error parsing JSON");
            }

            return result;
        }
    }

    @TypeConverter
    public static @Nullable String minforecastArrtoJson(@Nullable List<MinutelyForecast> value) {
        if (value == null)
            return null;
        else {
            Buffer buffer = new Buffer();
            JsonWriter writer = JsonWriter.of(buffer);
            writer.setSerializeNulls(true);

            try {
                writer.beginArray();

                for (MinutelyForecast forecast : value) {
                    forecast.toJson(writer);
                }

                writer.endArray();
                writer.close();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error writing JSON");
            }

            return buffer.readUtf8();
        }
    }

    @TypeConverter
    public static @Nullable Condition conditionFromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            Condition obj = new Condition();
            obj.fromJson(JsonReader.of(new Buffer().writeUtf8(value)));
            return obj;
        }
    }

    @TypeConverter
    public static @Nullable String conditionToJson(@Nullable Condition value) {
        return JSONParser.serializer(value, Condition.class);
    }

    @TypeConverter
    public static @Nullable Atmosphere atmosphereFromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            Atmosphere obj = new Atmosphere();
            obj.fromJson(JsonReader.of(new Buffer().writeUtf8(value)));
            return obj;
        }
    }

    @TypeConverter
    public static @Nullable String atmosphereToJson(@Nullable Atmosphere value) {
        return JSONParser.serializer(value, Atmosphere.class);
    }

    @TypeConverter
    public static @Nullable Astronomy astronomyFromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            Astronomy obj = new Astronomy();
            obj.fromJson(JsonReader.of(new Buffer().writeUtf8(value)));
            return obj;
        }
    }

    @TypeConverter
    public static @Nullable String astronomyToJson(@Nullable Astronomy value) {
        return JSONParser.serializer(value, Astronomy.class);
    }

    @TypeConverter
    public static @Nullable Precipitation precipitationFromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            Precipitation obj = new Precipitation();
            obj.fromJson(JsonReader.of(new Buffer().writeUtf8(value)));
            return obj;
        }
    }

    @TypeConverter
    public static @Nullable String precipitationToJson(@Nullable Precipitation value) {
        return JSONParser.serializer(value, Precipitation.class);
    }

    @TypeConverter
    public static @Nullable Collection<WeatherAlert> alertsListFromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            JsonReader reader = JsonReader.of(new Buffer().writeUtf8(value));
            List<WeatherAlert> result = new ArrayList<>();

            try {
                reader.beginArray();

                while (reader.hasNext()) {
                    @SuppressLint("RestrictedApi") WeatherAlert obj = new WeatherAlert();
                    obj.fromJson(reader);
                    result.add(obj);
                }

                reader.endArray();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error parsing JSON");
            }

            return result;
        }
    }

    @TypeConverter
    public static @Nullable String alertsListToJson(@Nullable Collection<WeatherAlert> value) {
        if (value == null)
            return null;
        else {
            Buffer buffer = new Buffer();
            JsonWriter writer = JsonWriter.of(buffer);
            writer.setSerializeNulls(true);

            try {
                writer.beginArray();

                for (WeatherAlert alert : value) {
                    alert.toJson(writer);
                }

                writer.endArray();
                writer.close();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error writing JSON");
            }

            return buffer.readUtf8();
        }
    }

    @TypeConverter
    public static @Nullable AirQuality aqiFromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            AirQuality obj = new AirQuality();
            obj.fromJson(JsonReader.of(new Buffer().writeUtf8(value)));
            return obj;
        }
    }

    @TypeConverter
    public static @Nullable String aqiToJson(@Nullable AirQuality value) {
        return JSONParser.serializer(value, AirQuality.class);
    }

    @TypeConverter
    public static @Nullable List<AirQuality> aqiForecastArrfromJson(@Nullable String value) {
        if (value == null)
            return null;
        else {
            JsonReader reader = JsonReader.of(new Buffer().writeUtf8(value));
            List<AirQuality> result = new ArrayList<>(90);

            try {
                reader.beginArray();

                while (reader.hasNext()) {
                    AirQuality obj = new AirQuality();
                    obj.fromJson(reader);
                    result.add(obj);
                }

                reader.endArray();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error parsing JSON");
            }

            return result;
        }
    }

    @TypeConverter
    public static @Nullable String aqiForecastArrtoJson(@Nullable List<AirQuality> value) {
        if (value == null)
            return null;
        else {
            Buffer buffer = new Buffer();
            JsonWriter writer = JsonWriter.of(buffer);
            writer.setSerializeNulls(true);

            try {
                writer.beginArray();

                for (AirQuality aqi : value) {
                    aqi.toJson(writer);
                }

                writer.endArray();
                writer.close();
            } catch (IOException ex) {
                Logger.writeLine(Log.ERROR, ex, "Error writing JSON");
            }

            return buffer.readUtf8();
        }
    }
}