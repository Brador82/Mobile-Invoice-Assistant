package com.mobileinvoice.delivery.data.converters;

import androidx.room.TypeConverter;
import com.mobileinvoice.delivery.models.DeliveryStatus;
import com.mobileinvoice.delivery.models.Priority;

/**
 * Room TypeConverters for Enum types
 */
public class EnumConverters {
    
    @TypeConverter
    public static String fromDeliveryStatus(DeliveryStatus status) {
        return status == null ? null : status.name();
    }

    @TypeConverter
    public static DeliveryStatus toDeliveryStatus(String status) {
        return status == null ? null : DeliveryStatus.valueOf(status);
    }

    @TypeConverter
    public static String fromPriority(Priority priority) {
        return priority == null ? null : priority.name();
    }

    @TypeConverter
    public static Priority toPriority(String priority) {
        return priority == null ? null : Priority.valueOf(priority);
    }
}
