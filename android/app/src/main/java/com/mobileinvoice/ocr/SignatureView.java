package com.mobileinvoice.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.exifinterface.media.ExifInterface;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class SignatureView extends View {
    private Paint paint;
    private Path path;
    private Canvas canvas;
    private Bitmap bitmap;
    private Bitmap backgroundImage;
    private boolean hasDrawn = false;

    public SignatureView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(5f);
        
        path = new Path();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        canvas = new Canvas(bitmap);
        
        // Draw background - either invoice image or white
        if (backgroundImage != null) {
            drawBackgroundImage();
        } else {
            canvas.drawColor(Color.WHITE);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawBitmap(bitmap, 0, 0, null);
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                return true;
                
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                break;
                
            case MotionEvent.ACTION_UP:
                canvas.drawPath(path, paint);
                path.reset();
                hasDrawn = true;
                break;
                
            default:
                return false;
        }

        invalidate();
        return true;
    }

    public void clear() {
        path.reset();
        hasDrawn = false;
        if (bitmap != null) {
            // Restore background - either invoice image or white
            if (backgroundImage != null) {
                drawBackgroundImage();
            } else {
                canvas.drawColor(Color.WHITE);
            }
        }
        invalidate();
    }

    public boolean hasSignature() {
        return hasDrawn;
    }

    public void setBackgroundImage(String imagePath) {
        android.util.Log.d("SignatureView", "setBackgroundImage called with: " + imagePath);
        
        if (imagePath != null) {
            try {
                Bitmap loadedBitmap = null;
                
                // Check if it's a content:// URI
                if (imagePath.startsWith("content://")) {
                    android.util.Log.d("SignatureView", "Loading from content URI");
                    Uri uri = Uri.parse(imagePath);
                    
                    // Load bitmap from content URI using ContentResolver
                    loadedBitmap = MediaStore.Images.Media.getBitmap(
                        getContext().getContentResolver(), uri
                    );
                    
                    // Get EXIF rotation from content URI
                    if (loadedBitmap != null) {
                        android.util.Log.d("SignatureView", "Bitmap loaded from content URI successfully");
                        backgroundImage = rotateBitmapFromContentUri(loadedBitmap, uri);
                        
                        if (backgroundImage != loadedBitmap) {
                            loadedBitmap.recycle();
                        }
                    }
                } else {
                    // Handle file:// URIs and regular file paths
                    String filePath = imagePath.replace("file://", "");
                    android.util.Log.d("SignatureView", "Loading from file path: " + filePath);
                    
                    File imageFile = new File(filePath);
                    android.util.Log.d("SignatureView", "Image file exists: " + imageFile.exists());
                    
                    if (imageFile.exists()) {
                        // Load and scale the image
                        BitmapFactory.Options options = new BitmapFactory.Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(filePath, options);
                        
                        android.util.Log.d("SignatureView", "Image dimensions: " + options.outWidth + "x" + options.outHeight);
                        
                        // Calculate sample size for memory efficiency
                        options.inSampleSize = calculateInSampleSize(options, 1920, 1080);
                        options.inJustDecodeBounds = false;
                        
                        loadedBitmap = BitmapFactory.decodeFile(filePath, options);
                        
                        if (loadedBitmap != null) {
                            android.util.Log.d("SignatureView", "Bitmap loaded from file successfully");
                            backgroundImage = rotateBitmapIfNeeded(loadedBitmap, filePath);
                            
                            if (backgroundImage != loadedBitmap) {
                                loadedBitmap.recycle();
                            }
                        }
                    } else {
                        android.util.Log.e("SignatureView", "Image file does not exist: " + imageFile.getAbsolutePath());
                    }
                }
                
                // If we already have a canvas, draw the background immediately
                if (canvas != null && backgroundImage != null) {
                    drawBackgroundImage();
                    invalidate();
                } else {
                    android.util.Log.d("SignatureView", "Canvas not ready yet or no bitmap loaded");
                }
                
            } catch (IOException e) {
                android.util.Log.e("SignatureView", "Error loading image: " + e.getMessage());
            }
        } else {
            android.util.Log.d("SignatureView", "imagePath is null");
        }
    }
    
    /**
     * Read EXIF data and rotate bitmap from content URI
     */
    private Bitmap rotateBitmapFromContentUri(Bitmap bitmap, Uri uri) {
        try {
            InputStream inputStream = getContext().getContentResolver().openInputStream(uri);
            if (inputStream != null) {
                ExifInterface exif = new ExifInterface(inputStream);
                int orientation = exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION,
                    ExifInterface.ORIENTATION_NORMAL
                );
                
                inputStream.close();
                
                int rotation = 0;
                switch (orientation) {
                    case ExifInterface.ORIENTATION_ROTATE_90:
                        rotation = 90;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_180:
                        rotation = 180;
                        break;
                    case ExifInterface.ORIENTATION_ROTATE_270:
                        rotation = 270;
                        break;
                    case ExifInterface.ORIENTATION_NORMAL:
                    default:
                        return bitmap; // No rotation needed
                }
                
                // Apply rotation
                if (rotation != 0) {
                    Matrix matrix = new Matrix();
                    matrix.postRotate(rotation);
                    return Bitmap.createBitmap(bitmap, 0, 0, 
                        bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                }
            }
        } catch (IOException e) {
            android.util.Log.e("SignatureView", "Error reading EXIF from URI: " + e.getMessage());
        }
        
        return bitmap;
    }
    
    /**
     * Read EXIF data and rotate bitmap if needed
     */
    private Bitmap rotateBitmapIfNeeded(Bitmap bitmap, String imagePath) {
        try {
            ExifInterface exif = new ExifInterface(imagePath);
            int orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION,
                ExifInterface.ORIENTATION_NORMAL
            );
            
            int rotation = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    rotation = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    rotation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    rotation = 270;
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                default:
                    return bitmap; // No rotation needed
            }
            
            // Apply rotation
            if (rotation != 0) {
                Matrix matrix = new Matrix();
                matrix.postRotate(rotation);
                return Bitmap.createBitmap(bitmap, 0, 0, 
                    bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            }
            
        } catch (IOException e) {
            // If we can't read EXIF, just return the original bitmap
        }
        
        return bitmap;
    }
    
    private void drawBackgroundImage() {
        if (backgroundImage != null && canvas != null) {
            // Clear canvas first
            canvas.drawColor(Color.WHITE);
            
            // Scale image to fit the canvas while maintaining aspect ratio
            int canvasWidth = canvas.getWidth();
            int canvasHeight = canvas.getHeight();
            int imgWidth = backgroundImage.getWidth();
            int imgHeight = backgroundImage.getHeight();
            
            float scale = Math.min(
                (float) canvasWidth / imgWidth,
                (float) canvasHeight / imgHeight
            );
            
            int scaledWidth = (int) (imgWidth * scale);
            int scaledHeight = (int) (imgHeight * scale);
            
            // Center the image
            int left = (canvasWidth - scaledWidth) / 2;
            int top = (canvasHeight - scaledHeight) / 2;
            
            Rect destRect = new Rect(left, top, left + scaledWidth, top + scaledHeight);
            
            // Add slight transparency to the background so signature is more visible
            Paint bgPaint = new Paint();
            bgPaint.setAlpha(180); // 70% opacity
            canvas.drawBitmap(backgroundImage, null, destRect, bgPaint);
        }
    }
    
    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        
        return inSampleSize;
    }

    public Bitmap getSignatureBitmap() {
        return bitmap;
    }
}
