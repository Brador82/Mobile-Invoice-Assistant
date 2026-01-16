package com.mobileinvoice.ocr.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {Invoice.class}, version = 2, exportSchema = false)
public abstract class InvoiceDatabase extends RoomDatabase {
    private static InvoiceDatabase instance;

    public abstract InvoiceDao invoiceDao();

    public static synchronized InvoiceDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    InvoiceDatabase.class,
                    "invoice_database"
            )
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
