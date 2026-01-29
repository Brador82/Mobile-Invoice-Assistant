package com.mobileinvoice.ocr;

import android.content.Context;
import android.util.Log;
import com.google.gson.Gson;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Parser for ACES invoice template JSON files
 * Loads template from assets and converts to Java objects
 */
public class TemplateParser {
    private static final String TAG = "TemplateParser";
    private Context context;
    
    public TemplateParser(Context context) {
        this.context = context;
    }
    
    /**
     * Load invoice template from assets
     * @param templateName Name of template file in assets (e.g., "invoice_template.json")
     * @return InvoiceTemplate object or null if loading fails
     */
    public InvoiceTemplate loadTemplate(String templateName) {
        try {
            InputStream is = context.getAssets().open(templateName);
            InputStreamReader reader = new InputStreamReader(is);
            
            Gson gson = new Gson();
            InvoiceTemplate template = gson.fromJson(reader, InvoiceTemplate.class);
            
            reader.close();
            is.close();
            
            Log.d(TAG, "Loaded template: " + templateName);
            Log.d(TAG, "Grid: " + template.grid.cols + "x" + template.grid.rows);
            Log.d(TAG, "Fields: " + template.fields.size());
            
            return template;
            
        } catch (Exception e) {
            Log.e(TAG, "Failed to load template: " + templateName, e);
            return null;
        }
    }
    
    /**
     * Get field from template by name
     */
    public InvoiceTemplate.Field getField(InvoiceTemplate template, String fieldName) {
        if (template == null || template.fields == null) {
            return null;
        }
        
        for (InvoiceTemplate.Field field : template.fields) {
            if (field.name.equals(fieldName)) {
                return field;
            }
        }
        
        return null;
    }
    
    /**
     * Get field from template by type
     */
    public InvoiceTemplate.Field getFieldByType(InvoiceTemplate template, String fieldType) {
        if (template == null || template.fields == null) {
            return null;
        }
        
        for (InvoiceTemplate.Field field : template.fields) {
            if (field.type.equals(fieldType)) {
                return field;
            }
        }
        
        return null;
    }
}
