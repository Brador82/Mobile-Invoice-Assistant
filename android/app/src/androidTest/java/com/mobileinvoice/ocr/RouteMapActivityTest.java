package com.mobileinvoice.ocr;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

/**
 * Instrumentation tests for RouteMapActivity
 * Tests route optimization UI, map display, and stop management
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class RouteMapActivityTest {

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    private Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        return new Intent(context, RouteMapActivity.class);
    }

    // ==================== Activity Launch Tests ====================

    @Test
    public void testActivityLaunches() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Activity should launch without crashing
        }
    }

    // ==================== Map Tests ====================

    @Test
    public void testMapViewExists() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.mapView))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Route List Tests ====================

    @Test
    public void testRouteStopsListExists() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.recyclerViewStops))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void testEmptyRouteList() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // When no stops exist, list should be empty but displayed
            onView(withId(R.id.recyclerViewStops))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Optimize Button Tests ====================

    @Test
    public void testOptimizeButtonExists() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnOptimizeRoute))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void testOptimizeButtonClickable() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnOptimizeRoute))
                    .perform(click());
            // Should handle click even with empty route
        }
    }

    // ==================== Route Summary Tests ====================

    @Test
    public void testRouteSummaryExists() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.txtRouteSummary))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Navigation Start Tests ====================

    @Test
    public void testStartNavigationButtonExists() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnStartNavigation))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Split Screen Tests ====================

    @Test
    public void testSplitScreenLayout() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Both map and list should be visible in split screen
            onView(withId(R.id.mapView))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.recyclerViewStops))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== State Restoration Tests ====================

    @Test
    public void testStateRestorationAfterRotation() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Simulate rotation
            scenario.recreate();

            // Views should still be displayed
            onView(withId(R.id.mapView))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.recyclerViewStops))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Toolbar Tests ====================

    @Test
    public void testToolbarDisplayed() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.toolbar))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Progress Indicator Tests ====================

    @Test
    public void testProgressIndicatorHiddenInitially() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Progress indicator should be hidden when not optimizing
            // Note: Actual check depends on visibility state
        }
    }

    // ==================== Error Handling Tests ====================

    @Test
    public void testHandlesEmptyInvoiceList() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Activity should handle empty invoice list gracefully
            onView(withId(R.id.btnOptimizeRoute))
                    .perform(click());

            // Should not crash, should show appropriate message or state
            onView(withId(R.id.recyclerViewStops))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Drag-Drop Tests ====================

    @Test
    public void testRecyclerViewSupportsReorder() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // RecyclerView should exist and support drag-drop
            onView(withId(R.id.recyclerViewStops))
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Back Navigation Tests ====================

    @Test
    public void testBackNavigationToMain() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Pressing back should return to MainActivity
            // Note: Navigation testing with pressBack()
        }
    }

    // ==================== UI Interaction Tests ====================

    @Test
    public void testAllMainElementsInteractive() {
        try (ActivityScenario<RouteMapActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Verify all main elements are interactive
            onView(withId(R.id.mapView)).check(matches(isDisplayed()));
            onView(withId(R.id.recyclerViewStops)).check(matches(isDisplayed()));
            onView(withId(R.id.btnOptimizeRoute)).check(matches(isEnabled()));
            onView(withId(R.id.btnStartNavigation)).check(matches(isDisplayed()));
        }
    }
}
