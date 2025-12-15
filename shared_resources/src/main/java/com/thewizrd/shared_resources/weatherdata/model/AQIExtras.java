package com.thewizrd.shared_resources.weatherdata.model;

import android.util.Log;

import androidx.annotation.NonNull;

import com.squareup.moshi.Json;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.thewizrd.shared_resources.utils.CustomJsonObject;
import com.thewizrd.shared_resources.utils.Logger;
import com.thewizrd.shared_resources.utils.NumberUtils;

import java.io.IOException;

import okio.Buffer;

public class AQIExtras extends CustomJsonObject {

    @Json(name = "o3_max")
    private Integer o3Max;

    @Json(name = "o3_min")
    private Integer o3Min;

    @Json(name = "pm25_max")
    private Integer pm25Max;

    @Json(name = "pm25_min")
    private Integer pm25Min;

    @Json(name = "pm10_max")
    private Integer pm10Max;

    @Json(name = "pm10_min")
    private Integer pm10Min;

    public Integer getO3Max() {
        return o3Max;
    }

    public void setO3Max(Integer o3Max) {
        this.o3Max = o3Max;
    }

    public Integer getO3Min() {
        return o3Min;
    }

    public void setO3Min(Integer o3Min) {
        this.o3Min = o3Min;
    }

    public Integer getPm25Max() {
        return pm25Max;
    }

    public void setPm25Max(Integer pm25Max) {
        this.pm25Max = pm25Max;
    }

    public Integer getPm25Min() {
        return pm25Min;
    }

    public void setPm25Min(Integer pm25Min) {
        this.pm25Min = pm25Min;
    }

    public Integer getPm10Max() {
        return pm10Max;
    }

    public void setPm10Max(Integer pm10Max) {
        this.pm10Max = pm10Max;
    }

    public Integer getPm10Min() {
        return pm10Min;
    }

    public void setPm10Min(Integer pm10Min) {
        this.pm10Min = pm10Min;
    }

    @Override
    public void fromJson(@NonNull JsonReader extReader) {
        try {
            JsonReader reader;
            String jsonValue;

            if (extReader.peek() == JsonReader.Token.STRING) {
                jsonValue = extReader.nextString();
            } else {
                jsonValue = null;
            }

            if (jsonValue == null)
                reader = extReader;
            else {
                reader = JsonReader.of(new Buffer().writeUtf8(jsonValue));
                reader.beginObject(); // StartObject
            }

            while (reader.hasNext() && reader.peek() != JsonReader.Token.END_OBJECT) {
                if (reader.peek() == JsonReader.Token.BEGIN_OBJECT)
                    reader.beginObject(); // StartObject

                String property = reader.nextName();

                if (reader.peek() == JsonReader.Token.NULL) {
                    reader.nextNull();
                    continue;
                }

                switch (property) {
                    case "o3_max":
                        this.o3Max = NumberUtils.tryParseInt(reader.nextString());
                        break;
                    case "o3_min":
                        this.o3Min = NumberUtils.tryParseInt(reader.nextString());
                        break;
                    case "pm25_max":
                        this.pm25Max = NumberUtils.tryParseInt(reader.nextString());
                        break;
                    case "pm25_min":
                        this.pm25Min = NumberUtils.tryParseInt(reader.nextString());
                        break;
                    case "pm10_max":
                        this.pm10Max = NumberUtils.tryParseInt(reader.nextString());
                        break;
                    case "pm10_min":
                        this.pm10Min = NumberUtils.tryParseInt(reader.nextString());
                        break;
                    default:
                        reader.skipValue();
                        break;
                }
            }

            if (reader.peek() == JsonReader.Token.END_OBJECT)
                reader.endObject();

        } catch (Exception ignored) {
        }
    }

    public void toJson(@NonNull JsonWriter writer) {
        try {
            // {
            writer.beginObject();

            // "o3_max" : ""
            writer.name("o3_max");
            writer.value(o3Max);

            // "o3_min" : ""
            writer.name("o3_min");
            writer.value(o3Min);

            // "pm25_max" : ""
            writer.name("pm25_max");
            writer.value(pm25Max);

            // "pm25_min" : ""
            writer.name("pm25_min");
            writer.value(pm25Min);

            // "pm10_max" : ""
            writer.name("pm10_max");
            writer.value(pm10Max);

            // "pm10_min" : ""
            writer.name("pm10_min");
            writer.value(pm10Min);

            // }
            writer.endObject();
        } catch (IOException e) {
            Logger.writeLine(Log.ERROR, e, "AQIExtras: error writing json string");
        }
    }
}