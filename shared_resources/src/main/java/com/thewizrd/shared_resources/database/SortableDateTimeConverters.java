package com.thewizrd.shared_resources.database;

import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import com.thewizrd.shared_resources.utils.DateTimeUtils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class SortableDateTimeConverters {
    private static final DateTimeFormatter zDTF = DateTimeUtils.ofPatternForInvariantLocale("yyyy-MM-dd HH:mm:ss ZZZZZ");
    private static final DateTimeFormatter lDTF = DateTimeFormatter.ISO_INSTANT.withLocale(Locale.ROOT);

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
}
