package com.mobileinvoice.ocr.database;

import androidx.room.TypeConverter;

public class Converters {
    @TypeConverter
    public static String fromLong(Long value) {
        return value == null ? null : String.valueOf(value);
    }

    @TypeConverter
    public static Long toLong(String value) {
        return value == null ? null : Long.parseLong(value);
    }
}
