package com.mobileinvoice.ocr.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface InvoiceDao {
    @Insert
    long insert(Invoice invoice);

    @Update
    void update(Invoice invoice);

    @Delete
    void delete(Invoice invoice);

    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    LiveData<List<Invoice>> getAllInvoices();

    @Query("SELECT * FROM invoices WHERE id = :id")
    LiveData<Invoice> getInvoiceById(int id);

    @Query("SELECT * FROM invoices WHERE id = :id")
    Invoice getInvoiceByIdSync(int id);

    @Query("SELECT * FROM invoices ORDER BY timestamp DESC")
    List<Invoice> getAllInvoicesSync();

    @Query("DELETE FROM invoices")
    void deleteAll();

    @Query("SELECT COUNT(*) FROM invoices")
    LiveData<Integer> getInvoiceCount();
}
