package com.mobileinvoice.ocr.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {Invoice.class}, version = 4, exportSchema = false)
public abstract class InvoiceDatabase extends RoomDatabase {
    private static InvoiceDatabase instance;

    public abstract InvoiceDao invoiceDao();

    // Migration from version 2 to 3: Add status column
    private static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Add status column with default value PENDING (nullable to match entity)
            database.execSQL("ALTER TABLE invoices ADD COLUMN status TEXT DEFAULT 'PENDING'");
        }
    };

    // Migration from version 3 to 4: Fix status column constraint mismatch
    // Recreate table with correct schema (nullable status column)
    private static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Create new table with correct schema
            database.execSQL("CREATE TABLE invoices_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "invoiceNumber TEXT, " +
                    "customerName TEXT, " +
                    "address TEXT, " +
                    "phone TEXT, " +
                    "items TEXT, " +
                    "podImagePath1 TEXT, " +
                    "podImagePath2 TEXT, " +
                    "podImagePath3 TEXT, " +
                    "signatureImagePath TEXT, " +
                    "notes TEXT, " +
                    "originalImagePath TEXT, " +
                    "rawOcrText TEXT, " +
                    "timestamp INTEGER NOT NULL, " +
                    "status TEXT)");
            
            // Copy data from old table
            database.execSQL("INSERT INTO invoices_new (id, invoiceNumber, customerName, address, " +
                    "phone, items, podImagePath1, podImagePath2, podImagePath3, signatureImagePath, " +
                    "notes, originalImagePath, rawOcrText, timestamp, status) " +
                    "SELECT id, invoiceNumber, customerName, address, phone, items, podImagePath1, " +
                    "podImagePath2, podImagePath3, signatureImagePath, notes, originalImagePath, " +
                    "rawOcrText, timestamp, status FROM invoices");
            
            // Drop old table
            database.execSQL("DROP TABLE invoices");
            
            // Rename new table to original name
            database.execSQL("ALTER TABLE invoices_new RENAME TO invoices");
        }
    };

    public static synchronized InvoiceDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                    context.getApplicationContext(),
                    InvoiceDatabase.class,
                    "invoice_database"
            )
                    .addMigrations(MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
