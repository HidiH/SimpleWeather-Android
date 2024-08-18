package com.thewizrd.shared_resources.utils;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.StringDef;

import com.thewizrd.shared_resources.R;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public final class Units {
    @StringDef({
            FAHRENHEIT,
            CELSIUS
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface TemperatureUnits {
    }

    @StringDef({
            MILES_PER_HOUR,
            KILOMETERS_PER_HOUR,
            METERS_PER_SECOND,
            KNOTS
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface SpeedUnits {
    }

    @StringDef({
            INHG,
            MILLIBAR,
            MMHG
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PressureUnits {
    }

    @StringDef({
            MILES,
            KILOMETERS
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface DistanceUnits {
    }

    @StringDef({
            INCHES,
            MILLIMETERS
    })
    @Retention(RetentionPolicy.SOURCE)
    public @interface PrecipitationUnits {
    }

    public static final String FAHRENHEIT = "F";
    public static final String CELSIUS = "C";
    public static final String MILES_PER_HOUR = "MPH";
    public static final String KILOMETERS_PER_HOUR = "KMPH";
    public static final String METERS_PER_SECOND = "MSEC";
    public static final String KNOTS = "KNOTS";
    public static final String INHG = "INMERCURY";
    public static final String MILLIBAR = "MILLIBAR";
    public static final String MMHG = "MMMERCURY";
    public static final String MILES = "MILES";
    public static final String KILOMETERS = "KILOMETERS";
    public static final String INCHES = "INCHES";
    public static final String MILLIMETERS = "MILLIMETERS";

    public static String getUnitString(@NonNull Context context, @NonNull String unit) {
        return switch (unit) {
            case MILES_PER_HOUR -> context.getString(R.string.unit_mph);
            case KILOMETERS_PER_HOUR -> context.getString(R.string.unit_kph);
            case METERS_PER_SECOND -> context.getString(R.string.unit_msec);
            case KNOTS -> context.getString(R.string.unit_knots);
            case INHG -> context.getString(R.string.unit_inHg);
            case MILLIBAR -> context.getString(R.string.unit_mBar);
            case MMHG -> context.getString(R.string.unit_mmHg);
            case MILES -> context.getString(R.string.unit_miles);
            case KILOMETERS -> context.getString(R.string.unit_kilometers);
            case INCHES -> context.getString(R.string.unit_in);
            case MILLIMETERS -> context.getString(R.string.unit_mm);
            default -> unit;
        };
    }
}
