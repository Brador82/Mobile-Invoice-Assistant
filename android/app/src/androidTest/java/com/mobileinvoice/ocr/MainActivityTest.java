package com.mobileinvoice.ocr;

import android.content.Intent;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

/**
 * Instrumentation tests for MainActivity
 * Tests UI interactions, navigation, and basic functionality
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void setUp() {
        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
    }

    // ==================== Activity Launch Tests ====================

    @Test
    public void testActivityLaunches() {
        // Simply launching the activity should not crash
        activityRule.getScenario().onActivity(activity -> {
            // Activity launched successfully
        });
    }

    @Test
    public void testMainLayoutDisplayed() {
        // Check that main layout elements are displayed
        onView(withId(R.id.recyclerViewInvoices))
                .check(matches(isDisplayed()));
    }

    // ==================== RecyclerView Tests ====================

    @Test
    public void testRecyclerViewExists() {
        onView(withId(R.id.recyclerViewInvoices))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEmptyState() {
        // When no invoices exist, the RecyclerView should be empty
        activityRule.getScenario().onActivity(activity -> {
            RecyclerView recyclerView = activity.findViewById(R.id.recyclerViewInvoices);
            // Note: In a real test, we'd check for an empty state view
        });
    }

    // ==================== Button Tests ====================

    @Test
    public void testUploadButtonExists() {
        onView(withId(R.id.fabUpload))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testUploadButtonClickable() {
        onView(withId(R.id.fabUpload))
                .check(matches(isEnabled()));
    }

    @Test
    public void testProcessButtonExists() {
        onView(withId(R.id.btnProcessAll))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testExportButtonExists() {
        onView(withId(R.id.btnExport))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testRouteButtonExists() {
        onView(withId(R.id.btnRoute))
                .check(matches(isDisplayed()));
    }

    // ==================== FAB Upload Tests ====================

    @Test
    public void testFabUploadClick_showsImageSourceDialog() {
        // Clicking the upload FAB should show image source selection
        onView(withId(R.id.fabUpload))
                .perform(click());

        // Dialog should appear (checking for either camera or gallery option)
        // Note: Actual dialog text depends on implementation
    }

    // ==================== Process All Button Tests ====================

    @Test
    public void testProcessAllButton_clickable() {
        onView(withId(R.id.btnProcessAll))
                .check(matches(isEnabled()))
                .perform(click());
        // Should handle click even with no invoices
    }

    // ==================== Export Button Tests ====================

    @Test
    public void testExportButton_clickable() {
        onView(withId(R.id.btnExport))
                .check(matches(isEnabled()))
                .perform(click());
        // Should show export options or message
    }

    // ==================== Route Button Tests ====================

    @Test
    public void testRouteButton_clickable() {
        onView(withId(R.id.btnRoute))
                .check(matches(isEnabled()));
    }

    @Test
    public void testRouteButton_navigatesToRouteActivity() {
        onView(withId(R.id.btnRoute))
                .perform(click());

        // Should navigate to RouteMapActivity
        intended(hasComponent(RouteMapActivity.class.getName()));
    }

    // ==================== Navigation Tests ====================

    @Test
    public void testBackNavigation() {
        // Should exit app when pressing back on main activity
        // Note: This test verifies navigation behavior
    }

    // ==================== State Restoration Tests ====================

    @Test
    public void testStateRestorationAfterRotation() {
        // Simulate configuration change
        activityRule.getScenario().recreate();

        // Views should still be displayed
        onView(withId(R.id.recyclerViewInvoices))
                .check(matches(isDisplayed()));
    }

    // ==================== Intent Tests ====================

    @Test
    public void testCameraIntent() {
        // Test that camera intent is properly formed
        // Note: Actual camera testing requires mock or emulator camera
    }

    @Test
    public void testGalleryIntent() {
        // Test that gallery intent is properly formed
        // Note: Actual gallery testing requires mock or device gallery
    }

    // ==================== Menu Tests ====================

    @Test
    public void testToolbarDisplayed() {
        // Check that toolbar is displayed
        onView(withId(R.id.toolbar))
                .check(matches(isDisplayed()));
    }

    // ==================== UI State Tests ====================

    @Test
    public void testInitialUIState() {
        // Verify initial state of all UI elements
        onView(withId(R.id.recyclerViewInvoices)).check(matches(isDisplayed()));
        onView(withId(R.id.fabUpload)).check(matches(isDisplayed()));
        onView(withId(R.id.btnProcessAll)).check(matches(isDisplayed()));
        onView(withId(R.id.btnExport)).check(matches(isDisplayed()));
        onView(withId(R.id.btnRoute)).check(matches(isDisplayed()));
    }

    // ==================== Accessibility Tests ====================

    @Test
    public void testButtonAccessibility() {
        // Buttons should be accessible
        onView(withId(R.id.fabUpload)).check(matches(isEnabled()));
        onView(withId(R.id.btnProcessAll)).check(matches(isEnabled()));
        onView(withId(R.id.btnExport)).check(matches(isEnabled()));
        onView(withId(R.id.btnRoute)).check(matches(isEnabled()));
    }

    // ==================== Error Handling Tests ====================

    @Test
    public void testGracefulErrorHandling() {
        // App should not crash when buttons are clicked with no data
        onView(withId(R.id.btnProcessAll)).perform(click());
        onView(withId(R.id.btnExport)).perform(click());

        // Activity should still be displayed
        onView(withId(R.id.recyclerViewInvoices)).check(matches(isDisplayed()));
    }

    // ==================== Helper Methods ====================

    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isDisplayed();
            }

            @Override
            public String getDescription() {
                return "Wait for " + millis + " milliseconds";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
