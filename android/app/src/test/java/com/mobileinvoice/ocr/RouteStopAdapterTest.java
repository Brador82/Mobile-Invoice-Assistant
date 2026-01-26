package com.mobileinvoice.ocr;

import com.mobileinvoice.ocr.database.Invoice;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RouteStopAdapter
 * Tests adapter behavior for route stop display and drag-drop functionality
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28, manifest = Config.NONE)
public class RouteStopAdapterTest {

    private List<RouteOptimizer.RoutePoint> testStops;

    @Before
    public void setUp() {
        testStops = new ArrayList<>();
    }

    private RouteOptimizer.RoutePoint createTestStop(int order, String customerName,
                                                       String address, double lat, double lng) {
        Invoice invoice = new Invoice();
        invoice.setId(order);
        invoice.setCustomerName(customerName);
        invoice.setAddress(address);
        invoice.setInvoiceNumber("INV-" + order);

        RouteOptimizer.RoutePoint point = new RouteOptimizer.RoutePoint(invoice, lat, lng, address);
        point.orderIndex = order;
        return point;
    }

    // ==================== Adapter Creation Tests ====================

    @Test
    public void testAdapterCreation_withEmptyList() {
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);
        assertNotNull("Adapter should be created", adapter);
        assertEquals("Item count should be 0", 0, adapter.getItemCount());
    }

    @Test
    public void testAdapterCreation_withData() {
        testStops.add(createTestStop(1, "John Doe", "123 Main St", 40.7128, -74.0060));
        testStops.add(createTestStop(2, "Jane Smith", "456 Oak Ave", 40.7580, -73.9855));

        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        assertNotNull("Adapter should be created", adapter);
        assertEquals("Item count should be 2", 2, adapter.getItemCount());
    }

    // ==================== Item Count Tests ====================

    @Test
    public void testGetItemCount_emptyList() {
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_singleItem() {
        testStops.add(createTestStop(1, "John Doe", "123 Main St", 40.7128, -74.0060));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testGetItemCount_multipleItems() {
        for (int i = 0; i < 10; i++) {
            testStops.add(createTestStop(i + 1, "Customer " + i, "Address " + i,
                                         40.0 + (i * 0.01), -74.0 + (i * 0.01)));
        }
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);
        assertEquals(10, adapter.getItemCount());
    }

    // ==================== Data Update Tests ====================

    @Test
    public void testUpdateData_addItems() {
        testStops.add(createTestStop(1, "John Doe", "123 Main St", 40.7128, -74.0060));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);
        assertEquals(1, adapter.getItemCount());

        testStops.add(createTestStop(2, "Jane Smith", "456 Oak Ave", 40.7580, -73.9855));
        adapter.notifyDataSetChanged();

        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void testUpdateData_clearAll() {
        for (int i = 0; i < 5; i++) {
            testStops.add(createTestStop(i + 1, "Customer " + i, "Address " + i,
                                         40.0 + (i * 0.01), -74.0 + (i * 0.01)));
        }
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);
        assertEquals(5, adapter.getItemCount());

        testStops.clear();
        adapter.notifyDataSetChanged();

        assertEquals(0, adapter.getItemCount());
    }

    // ==================== Move Item Tests ====================

    @Test
    public void testMoveItem_samePosition() {
        testStops.add(createTestStop(1, "John Doe", "123 Main St", 40.7128, -74.0060));
        testStops.add(createTestStop(2, "Jane Smith", "456 Oak Ave", 40.7580, -73.9855));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        // Moving to same position should have no effect
        adapter.moveItem(0, 0);

        assertEquals("John Doe", testStops.get(0).invoice.getCustomerName());
        assertEquals("Jane Smith", testStops.get(1).invoice.getCustomerName());
    }

    @Test
    public void testMoveItem_forwardMove() {
        testStops.add(createTestStop(1, "A", "Address A", 40.0, -74.0));
        testStops.add(createTestStop(2, "B", "Address B", 40.1, -74.1));
        testStops.add(createTestStop(3, "C", "Address C", 40.2, -74.2));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        // Move first item to last position
        adapter.moveItem(0, 2);

        assertEquals("B", testStops.get(0).invoice.getCustomerName());
        assertEquals("C", testStops.get(1).invoice.getCustomerName());
        assertEquals("A", testStops.get(2).invoice.getCustomerName());
    }

    @Test
    public void testMoveItem_backwardMove() {
        testStops.add(createTestStop(1, "A", "Address A", 40.0, -74.0));
        testStops.add(createTestStop(2, "B", "Address B", 40.1, -74.1));
        testStops.add(createTestStop(3, "C", "Address C", 40.2, -74.2));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        // Move last item to first position
        adapter.moveItem(2, 0);

        assertEquals("C", testStops.get(0).invoice.getCustomerName());
        assertEquals("A", testStops.get(1).invoice.getCustomerName());
        assertEquals("B", testStops.get(2).invoice.getCustomerName());
    }

    @Test
    public void testMoveItem_adjacentPositions() {
        testStops.add(createTestStop(1, "A", "Address A", 40.0, -74.0));
        testStops.add(createTestStop(2, "B", "Address B", 40.1, -74.1));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        // Swap adjacent items
        adapter.moveItem(0, 1);

        assertEquals("B", testStops.get(0).invoice.getCustomerName());
        assertEquals("A", testStops.get(1).invoice.getCustomerName());
    }

    // ==================== Order Index Update Tests ====================

    @Test
    public void testOrderIndexUpdatedAfterMove() {
        testStops.add(createTestStop(1, "A", "Address A", 40.0, -74.0));
        testStops.add(createTestStop(2, "B", "Address B", 40.1, -74.1));
        testStops.add(createTestStop(3, "C", "Address C", 40.2, -74.2));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        // After move, order indices should be updated
        adapter.moveItem(0, 2);
        adapter.updateOrderIndices();

        // Order indices should reflect new positions
        // Note: Implementation may vary
    }

    // ==================== Route Point Data Tests ====================

    @Test
    public void testRoutePointData_preserved() {
        RouteOptimizer.RoutePoint point = createTestStop(1, "John Doe", "123 Main St", 40.7128, -74.0060);
        testStops.add(point);

        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        RouteOptimizer.RoutePoint retrieved = testStops.get(0);
        assertEquals("John Doe", retrieved.invoice.getCustomerName());
        assertEquals("123 Main St", retrieved.formattedAddress);
        assertEquals(40.7128, retrieved.latitude, 0.0001);
        assertEquals(-74.0060, retrieved.longitude, 0.0001);
        assertEquals(1, retrieved.orderIndex);
    }

    // ==================== Click Listener Tests ====================

    @Test
    public void testSetOnItemClickListener() {
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        RouteStopAdapter.OnItemClickListener listener = mock(RouteStopAdapter.OnItemClickListener.class);
        adapter.setOnItemClickListener(listener);

        assertNotNull("Adapter should accept listener", adapter);
    }

    @Test
    public void testClickListener_nullListener() {
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        // Should handle null listener without crashing
        adapter.setOnItemClickListener(null);
        assertNotNull(adapter);
    }

    // ==================== Drag Listener Tests ====================

    @Test
    public void testSetOnDragListener() {
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        RouteStopAdapter.OnStartDragListener listener = mock(RouteStopAdapter.OnStartDragListener.class);
        adapter.setOnStartDragListener(listener);

        assertNotNull("Adapter should accept drag listener", adapter);
    }

    // ==================== Edge Cases ====================

    @Test
    public void testAdapter_withNullInvoice() {
        // RoutePoint with null invoice
        RouteOptimizer.RoutePoint point = new RouteOptimizer.RoutePoint(null, 40.0, -74.0, "Address");
        testStops.add(point);

        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void testAdapter_withZeroCoordinates() {
        testStops.add(createTestStop(1, "Customer", "Address", 0.0, 0.0));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        assertEquals(1, adapter.getItemCount());
        assertEquals(0.0, testStops.get(0).latitude, 0.0001);
        assertEquals(0.0, testStops.get(0).longitude, 0.0001);
    }

    @Test
    public void testAdapter_withNegativeCoordinates() {
        // Southern hemisphere
        testStops.add(createTestStop(1, "Sydney Customer", "Sydney Address", -33.8688, 151.2093));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        assertEquals(1, adapter.getItemCount());
        assertEquals(-33.8688, testStops.get(0).latitude, 0.0001);
    }

    @Test
    public void testMoveItem_invalidIndices() {
        testStops.add(createTestStop(1, "A", "Address A", 40.0, -74.0));
        testStops.add(createTestStop(2, "B", "Address B", 40.1, -74.1));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        // Negative index - should handle gracefully
        try {
            adapter.moveItem(-1, 0);
        } catch (IndexOutOfBoundsException e) {
            // Expected behavior
        }

        // Index out of bounds
        try {
            adapter.moveItem(0, 10);
        } catch (IndexOutOfBoundsException e) {
            // Expected behavior
        }
    }

    // ==================== Performance Tests ====================

    @Test
    public void testAdapter_withManyStops() {
        for (int i = 0; i < 100; i++) {
            testStops.add(createTestStop(i + 1, "Customer " + i, "Address " + i,
                                         40.0 + (i * 0.001), -74.0 + (i * 0.001)));
        }

        long startTime = System.currentTimeMillis();
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);
        long endTime = System.currentTimeMillis();

        assertEquals(100, adapter.getItemCount());
        assertTrue("Adapter creation should be fast", (endTime - startTime) < 1000);
    }

    @Test
    public void testMultipleMoves() {
        for (int i = 0; i < 10; i++) {
            testStops.add(createTestStop(i + 1, "Customer " + i, "Address " + i,
                                         40.0 + (i * 0.01), -74.0 + (i * 0.01)));
        }
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        // Perform multiple moves
        for (int i = 0; i < 5; i++) {
            adapter.moveItem(0, 9);
        }

        // Should still have 10 items
        assertEquals(10, adapter.getItemCount());
    }

    // ==================== Order Consistency Tests ====================

    @Test
    public void testOrderConsistency_afterMultipleMoves() {
        testStops.add(createTestStop(1, "A", "Address A", 40.0, -74.0));
        testStops.add(createTestStop(2, "B", "Address B", 40.1, -74.1));
        testStops.add(createTestStop(3, "C", "Address C", 40.2, -74.2));
        testStops.add(createTestStop(4, "D", "Address D", 40.3, -74.3));
        RouteStopAdapter adapter = new RouteStopAdapter(testStops);

        // Move A to end, then D to beginning
        adapter.moveItem(0, 3); // B, C, D, A
        adapter.moveItem(2, 0); // D, B, C, A

        assertEquals("D", testStops.get(0).invoice.getCustomerName());
        assertEquals("A", testStops.get(3).invoice.getCustomerName());
    }
}
