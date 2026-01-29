package com.mobileinvoice.ocr;

import java.util.List;

/**
 * Data classes for ACES (Anchored Coordinate Extraction System) invoice template
 * Template uses a grid-based coordinate system for precise field extraction
 */
public class InvoiceTemplate {
    
    public Grid grid;
    public int reference_width;
    public int reference_height;
    public List<Field> fields;
    
    public static class Grid {
        public int cols;
        public int rows;
    }
    
    public static class Field {
        public String name;
        public String type;
        public Coords coords;
        public String outputColumn;
        
        /**
         * Convert grid coordinates to pixel coordinates for a given image size
         */
        public android.graphics.Rect toPixelRect(int imageWidth, int imageHeight, Grid grid) {
            int left = (int)((coords.x1 / (double)grid.cols) * imageWidth);
            int top = (int)((coords.y1 / (double)grid.rows) * imageHeight);
            int right = (int)((coords.x2 / (double)grid.cols) * imageWidth);
            int bottom = (int)((coords.y2 / (double)grid.rows) * imageHeight);
            return new android.graphics.Rect(left, top, right, bottom);
        }
    }
    
    public static class Coords {
        public int x1;
        public int y1;
        public int x2;
        public int y2;
    }
}
