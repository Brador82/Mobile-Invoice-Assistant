package com.mobileinvoice.delivery.utils;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import androidx.core.content.ContextCompat;

import com.mobileinvoice.delivery.data.entities.Delivery;

/**
 * Helper class for common delivery operations
 */
public class DeliveryHelper {
    
    /**
     * Start phone call to customer
     */
    public static void callCustomer(Context context, Delivery delivery) {
        if (delivery.getCustomerPhone() == null || delivery.getCustomerPhone().isEmpty()) {
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel:" + delivery.getCustomerPhone()));
        
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
    
    /**
     * Start navigation to delivery address
     */
    public static void navigateToDelivery(Context context, Delivery delivery) {
        if (!delivery.hasCoordinates()) {
            // Try address-based navigation
            if (delivery.getFullAddress() != null && !delivery.getFullAddress().isEmpty()) {
                navigateToAddress(context, delivery.getFullAddress());
            }
            return;
        }
        
        // Use Google Maps with coordinates
        String uri = String.format("google.navigation:q=%f,%f", 
            delivery.getLatitude(), 
            delivery.getLongitude()
        );
        
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            // Fallback to browser-based maps
            String browserUri = String.format(
                "https://www.google.com/maps/dir/?api=1&destination=%f,%f",
                delivery.getLatitude(), 
                delivery.getLongitude()
            );
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri));
            context.startActivity(browserIntent);
        }
    }
    
    /**
     * Navigate using address string
     */
    public static void navigateToAddress(Context context, String address) {
        String uri = "google.navigation:q=" + Uri.encode(address);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        intent.setPackage("com.google.android.apps.maps");
        
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        } else {
            String browserUri = "https://www.google.com/maps/dir/?api=1&destination=" + Uri.encode(address);
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(browserUri));
            context.startActivity(browserIntent);
        }
    }
    
    /**
     * Send SMS to customer
     */
    public static void sendSMS(Context context, Delivery delivery, String message) {
        if (delivery.getCustomerPhone() == null || delivery.getCustomerPhone().isEmpty()) {
            return;
        }
        
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + delivery.getCustomerPhone()));
        intent.putExtra("sms_body", message);
        
        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(intent);
        }
    }
    
    /**
     * Format phone number for display
     */
    public static String formatPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return "";
        }
        
        // Remove non-digits
        String digits = phone.replaceAll("[^0-9]", "");
        
        // Format as (XXX) XXX-XXXX
        if (digits.length() == 10) {
            return String.format("(%s) %s-%s",
                digits.substring(0, 3),
                digits.substring(3, 6),
                digits.substring(6, 10)
            );
        }
        
        return phone;
    }
    
    /**
     * Check if location permission is granted
     */
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, 
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }
    
    /**
     * Get estimated delivery time string
     */
    public static String getETAString(Delivery delivery) {
        if (delivery.getEstimatedArrival() == null) {
            return "No ETA";
        }
        
        long now = System.currentTimeMillis();
        long eta = delivery.getEstimatedArrival().getTime();
        long diff = eta - now;
        
        if (diff < 0) {
            return "Overdue";
        }
        
        long minutes = diff / (60 * 1000);
        
        if (minutes < 60) {
            return minutes + " min";
        }
        
        long hours = minutes / 60;
        minutes = minutes % 60;
        
        return hours + "h " + minutes + "m";
    }
}
