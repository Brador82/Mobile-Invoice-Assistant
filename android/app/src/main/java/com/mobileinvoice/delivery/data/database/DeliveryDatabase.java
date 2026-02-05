package com.mobileinvoice.delivery.data.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.mobileinvoice.delivery.data.entities.Delivery;
import com.mobileinvoice.delivery.data.dao.DeliveryDao;
import com.mobileinvoice.delivery.data.converters.DateConverter;
import com.mobileinvoice.delivery.data.converters.EnumConverters;

/**
 * Room Database for Delivery Management
 * 
 * Features:
 * - Singleton pattern for single instance
 * - Type converters for Date and Enum types
 * - Automatic migration support (when needed)
 * - Thread-safe database access
 */
@Database(
    entities = {Delivery.class},
    version = 1,
    exportSchema = false
)
@TypeConverters({DateConverter.class, EnumConverters.class})
public abstract class DeliveryDatabase extends RoomDatabase {
    
    private static final String DATABASE_NAME = "delivery_database";
    private static volatile DeliveryDatabase INSTANCE;
    
    public abstract DeliveryDao deliveryDao();
    
    /**
     * Get singleton instance of the database
     */
    public static DeliveryDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (DeliveryDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        DeliveryDatabase.class,
                        DATABASE_NAME
                    )
                    // Uncomment for debugging - not recommended for production
                    // .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
    
    /**
     * Close database (typically not needed, Room handles this)
     */
    public static void destroyInstance() {
        if (INSTANCE != null && INSTANCE.isOpen()) {
            INSTANCE.close();
        }
        INSTANCE = null;
    }
}
