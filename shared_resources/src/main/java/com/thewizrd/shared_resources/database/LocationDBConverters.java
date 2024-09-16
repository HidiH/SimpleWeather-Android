package com.thewizrd.shared_resources.database;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import com.thewizrd.shared_resources.weatherdata.model.LocationType;

public class LocationDBConverters {

    @TypeConverter
    public static @NonNull LocationType locationTypeFromInt(int value) {
        return LocationType.valueOf(value);
    }

    @TypeConverter
    public static int locationTypeToInt(@NonNull LocationType value) {
        return value.getValue();
    }
}
