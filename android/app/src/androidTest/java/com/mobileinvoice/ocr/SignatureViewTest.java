package com.mobileinvoice.ocr;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.MotionEvent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

/**
 * Instrumentation tests for SignatureView
 * Tests the custom canvas view for signature capture
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
public class SignatureViewTest {

    private Context context;
    private SignatureView signatureView;

    @Before
    public void setUp() {
        context = ApplicationProvider.getApplicationContext();
        signatureView = new SignatureView(context, null);
    }

    // ==================== View Creation Tests ====================

    @Test
    public void testViewCreation() {
        assertNotNull("SignatureView should be created", signatureView);
    }

    @Test
    public void testViewCreation_withContext() {
        SignatureView view = new SignatureView(context, null);
        assertNotNull("SignatureView should be created with context", view);
    }

    // ==================== Initial State Tests ====================

    @Test
    public void testInitialState_isEmpty() {
        assertTrue("Signature should be empty initially", signatureView.isEmpty());
    }

    @Test
    public void testInitialState_noSignature() {
        // A newly created SignatureView should have no drawn content
        assertTrue("New view should have no signature", signatureView.isEmpty());
    }

    // ==================== Clear Tests ====================

    @Test
    public void testClear_emptyView() {
        // Clear on empty view should not crash
        signatureView.clear();
        assertTrue("View should remain empty after clear", signatureView.isEmpty());
    }

    @Test
    public void testClear_afterDrawing() {
        // Simulate drawing
        simulateDrawing(signatureView);

        // Clear should reset to empty
        signatureView.clear();
        assertTrue("View should be empty after clear", signatureView.isEmpty());
    }

    // ==================== Bitmap Generation Tests ====================

    @Test
    public void testGetSignatureBitmap_emptyView() {
        Bitmap bitmap = signatureView.getSignatureBitmap();

        // Should return a bitmap even if empty
        // Note: Behavior depends on implementation
        // Some implementations return null, others return empty bitmap
    }

    @Test
    public void testGetSignatureBitmap_afterDrawing() {
        // Simulate drawing
        simulateDrawing(signatureView);

        Bitmap bitmap = signatureView.getSignatureBitmap();
        assertNotNull("Should return bitmap after drawing", bitmap);
    }

    @Test
    public void testGetSignatureBitmap_dimensions() {
        // Force a size
        signatureView.layout(0, 0, 500, 300);

        simulateDrawing(signatureView);
        Bitmap bitmap = signatureView.getSignatureBitmap();

        if (bitmap != null) {
            assertTrue("Bitmap width should be positive", bitmap.getWidth() > 0);
            assertTrue("Bitmap height should be positive", bitmap.getHeight() > 0);
        }
    }

    // ==================== isEmpty Tests ====================

    @Test
    public void testIsEmpty_initialState() {
        assertTrue("New view should be empty", signatureView.isEmpty());
    }

    @Test
    public void testIsEmpty_afterClear() {
        simulateDrawing(signatureView);
        signatureView.clear();
        assertTrue("View should be empty after clear", signatureView.isEmpty());
    }

    @Test
    public void testIsEmpty_afterDrawing() {
        simulateDrawing(signatureView);
        assertFalse("View should not be empty after drawing", signatureView.isEmpty());
    }

    // ==================== Touch Event Tests ====================

    @Test
    public void testTouchEvent_down() {
        MotionEvent downEvent = MotionEvent.obtain(
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                MotionEvent.ACTION_DOWN,
                100f, 100f, 0
        );

        boolean handled = signatureView.onTouchEvent(downEvent);
        assertTrue("Touch down should be handled", handled);

        downEvent.recycle();
    }

    @Test
    public void testTouchEvent_move() {
        // First send down event
        MotionEvent downEvent = MotionEvent.obtain(
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                MotionEvent.ACTION_DOWN,
                100f, 100f, 0
        );
        signatureView.onTouchEvent(downEvent);
        downEvent.recycle();

        // Then send move event
        MotionEvent moveEvent = MotionEvent.obtain(
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                MotionEvent.ACTION_MOVE,
                150f, 150f, 0
        );

        boolean handled = signatureView.onTouchEvent(moveEvent);
        assertTrue("Touch move should be handled", handled);

        moveEvent.recycle();
    }

    @Test
    public void testTouchEvent_up() {
        // First send down event
        MotionEvent downEvent = MotionEvent.obtain(
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                MotionEvent.ACTION_DOWN,
                100f, 100f, 0
        );
        signatureView.onTouchEvent(downEvent);
        downEvent.recycle();

        // Then send up event
        MotionEvent upEvent = MotionEvent.obtain(
                System.currentTimeMillis(),
                System.currentTimeMillis(),
                MotionEvent.ACTION_UP,
                100f, 100f, 0
        );

        boolean handled = signatureView.onTouchEvent(upEvent);
        assertTrue("Touch up should be handled", handled);

        upEvent.recycle();
    }

    @Test
    public void testTouchSequence_drawsLine() {
        // Simulate a complete touch sequence
        long now = System.currentTimeMillis();

        // Down
        MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 50f, 50f, 0);
        signatureView.onTouchEvent(down);
        down.recycle();

        // Move (multiple points)
        for (int i = 1; i <= 10; i++) {
            MotionEvent move = MotionEvent.obtain(now, now + i * 10,
                    MotionEvent.ACTION_MOVE, 50f + i * 10, 50f + i * 5, 0);
            signatureView.onTouchEvent(move);
            move.recycle();
        }

        // Up
        MotionEvent up = MotionEvent.obtain(now, now + 110, MotionEvent.ACTION_UP, 150f, 100f, 0);
        signatureView.onTouchEvent(up);
        up.recycle();

        // View should no longer be empty
        assertFalse("View should have content after drawing", signatureView.isEmpty());
    }

    // ==================== Multiple Stroke Tests ====================

    @Test
    public void testMultipleStrokes() {
        // Draw first stroke
        simulateStroke(signatureView, 50f, 50f, 100f, 100f);

        // Draw second stroke
        simulateStroke(signatureView, 150f, 50f, 200f, 100f);

        assertFalse("View should have content after multiple strokes", signatureView.isEmpty());
    }

    @Test
    public void testClearBetweenStrokes() {
        // Draw first stroke
        simulateStroke(signatureView, 50f, 50f, 100f, 100f);
        assertFalse("Should have content after first stroke", signatureView.isEmpty());

        // Clear
        signatureView.clear();
        assertTrue("Should be empty after clear", signatureView.isEmpty());

        // Draw second stroke
        simulateStroke(signatureView, 150f, 50f, 200f, 100f);
        assertFalse("Should have content after second stroke", signatureView.isEmpty());
    }

    // ==================== Edge Cases ====================

    @Test
    public void testTouchAtOrigin() {
        simulateStroke(signatureView, 0f, 0f, 10f, 10f);
        assertFalse("Drawing at origin should work", signatureView.isEmpty());
    }

    @Test
    public void testSinglePoint() {
        // Just touch down and up at same point
        long now = System.currentTimeMillis();

        MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, 100f, 100f, 0);
        signatureView.onTouchEvent(down);
        down.recycle();

        MotionEvent up = MotionEvent.obtain(now, now + 10, MotionEvent.ACTION_UP, 100f, 100f, 0);
        signatureView.onTouchEvent(up);
        up.recycle();

        // Single point might or might not register as content depending on implementation
        // This test just ensures it doesn't crash
    }

    @Test
    public void testRapidTouches() {
        // Rapid succession of touch events
        for (int i = 0; i < 20; i++) {
            simulateStroke(signatureView,
                    (float) (i * 10), (float) (i * 5),
                    (float) (i * 10 + 20), (float) (i * 5 + 20));
        }

        assertFalse("Should have content after rapid touches", signatureView.isEmpty());
    }

    // ==================== Layout Tests ====================

    @Test
    public void testViewWithLayout() {
        signatureView.layout(0, 0, 400, 200);

        assertEquals("Width should be set", 400, signatureView.getWidth());
        assertEquals("Height should be set", 200, signatureView.getHeight());
    }

    @Test
    public void testDrawingWithinBounds() {
        signatureView.layout(0, 0, 400, 200);

        // Draw within bounds
        simulateStroke(signatureView, 50f, 50f, 350f, 150f);

        assertFalse("Drawing within bounds should work", signatureView.isEmpty());
    }

    // ==================== Helper Methods ====================

    private void simulateDrawing(SignatureView view) {
        simulateStroke(view, 50f, 50f, 150f, 100f);
    }

    private void simulateStroke(SignatureView view, float startX, float startY,
                                float endX, float endY) {
        long now = System.currentTimeMillis();

        // Down
        MotionEvent down = MotionEvent.obtain(now, now, MotionEvent.ACTION_DOWN, startX, startY, 0);
        view.onTouchEvent(down);
        down.recycle();

        // Move to end point
        int steps = 5;
        for (int i = 1; i <= steps; i++) {
            float x = startX + (endX - startX) * i / steps;
            float y = startY + (endY - startY) * i / steps;
            MotionEvent move = MotionEvent.obtain(now, now + i * 10,
                    MotionEvent.ACTION_MOVE, x, y, 0);
            view.onTouchEvent(move);
            move.recycle();
        }

        // Up
        MotionEvent up = MotionEvent.obtain(now, now + (steps + 1) * 10,
                MotionEvent.ACTION_UP, endX, endY, 0);
        view.onTouchEvent(up);
        up.recycle();
    }
}
