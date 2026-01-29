package com.mobileinvoice.delivery.data.converters;

import androidx.room.TypeConverter;
import java.util.Date;

/**
 * Room TypeConverter for Date objects
 */
public class DateConverter {
    @TypeConverter
    public static Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public static Long dateToTimestamp(Date date) {
        return date == null ? null : date.getTime();
    }
}
