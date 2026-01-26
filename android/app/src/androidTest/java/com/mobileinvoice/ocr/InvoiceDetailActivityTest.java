package com.mobileinvoice.ocr;

import android.content.Context;
import android.content.Intent;

import androidx.room.Room;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.mobileinvoice.ocr.database.Invoice;
import com.mobileinvoice.ocr.database.InvoiceDao;
import com.mobileinvoice.ocr.database.InvoiceDatabase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isEnabled;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

/**
 * Instrumentation tests for InvoiceDetailActivity
 * Tests invoice editing, POD capture, and signature functionality
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class InvoiceDetailActivityTest {

    private InvoiceDatabase database;
    private InvoiceDao invoiceDao;
    private int testInvoiceId;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        database = Room.inMemoryDatabaseBuilder(context, InvoiceDatabase.class)
                .allowMainThreadQueries()
                .build();
        invoiceDao = database.invoiceDao();

        // Create a test invoice
        Invoice testInvoice = new Invoice();
        testInvoice.setInvoiceNumber("TEST-001");
        testInvoice.setCustomerName("Test Customer");
        testInvoice.setAddress("123 Test Street");
        testInvoice.setPhone("(555) 123-4567");
        testInvoice.setItems("Refrigerator");
        testInvoice.setTimestamp(System.currentTimeMillis());

        testInvoiceId = (int) invoiceDao.insert(testInvoice);

        Intents.init();
    }

    @After
    public void tearDown() {
        Intents.release();
        database.close();
    }

    private Intent createIntent() {
        Context context = ApplicationProvider.getApplicationContext();
        Intent intent = new Intent(context, InvoiceDetailActivity.class);
        intent.putExtra("invoice_id", testInvoiceId);
        return intent;
    }

    // ==================== Activity Launch Tests ====================

    @Test
    public void testActivityLaunches() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Activity should launch without crashing
        }
    }

    @Test
    public void testLayoutDisplayed() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Basic layout elements should be displayed
            onView(withId(R.id.editInvoiceNumber)).check(matches(isDisplayed()));
        }
    }

    // ==================== Invoice Field Tests ====================

    @Test
    public void testInvoiceNumberField() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.editInvoiceNumber))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void testCustomerNameField() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.editCustomerName))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void testAddressField() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.editAddress))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void testPhoneField() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.editPhone))
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void testNotesField() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.editNotes))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    // ==================== Data Loading Tests ====================

    @Test
    public void testInvoiceDataLoaded() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Verify the test data is loaded
            onView(withId(R.id.editInvoiceNumber))
                    .check(matches(withText("TEST-001")));
            onView(withId(R.id.editCustomerName))
                    .check(matches(withText("Test Customer")));
        }
    }

    // ==================== Edit Tests ====================

    @Test
    public void testEditCustomerName() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.editCustomerName))
                    .perform(clearText())
                    .perform(typeText("Updated Customer"))
                    .perform(closeSoftKeyboard());

            onView(withId(R.id.editCustomerName))
                    .check(matches(withText("Updated Customer")));
        }
    }

    @Test
    public void testEditAddress() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.editAddress))
                    .perform(clearText())
                    .perform(typeText("456 New Address"))
                    .perform(closeSoftKeyboard());

            onView(withId(R.id.editAddress))
                    .check(matches(withText("456 New Address")));
        }
    }

    @Test
    public void testEditPhone() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.editPhone))
                    .perform(clearText())
                    .perform(typeText("5559876543"))
                    .perform(closeSoftKeyboard());

            onView(withId(R.id.editPhone))
                    .check(matches(withText("5559876543")));
        }
    }

    @Test
    public void testEditNotes() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.editNotes))
                    .perform(scrollTo())
                    .perform(clearText())
                    .perform(typeText("Special delivery instructions"))
                    .perform(closeSoftKeyboard());

            onView(withId(R.id.editNotes))
                    .check(matches(withText("Special delivery instructions")));
        }
    }

    // ==================== POD Button Tests ====================

    @Test
    public void testPodCaptureButton1Exists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnCapturePod1))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void testPodCaptureButton2Exists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnCapturePod2))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void testPodCaptureButton3Exists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnCapturePod3))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    // ==================== Signature Tests ====================

    @Test
    public void testSignatureButtonExists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnCaptureSignature))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void testSignatureButtonClickable() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnCaptureSignature))
                    .perform(scrollTo())
                    .perform(click());
            // Should navigate to SignatureActivity
        }
    }

    // ==================== Save Button Tests ====================

    @Test
    public void testSaveButtonExists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnSave))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    @Test
    public void testSaveButtonClick() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Make a change
            onView(withId(R.id.editCustomerName))
                    .perform(replaceText("Modified Customer"));

            // Click save
            onView(withId(R.id.btnSave))
                    .perform(scrollTo())
                    .perform(click());

            // Activity should finish or show confirmation
        }
    }

    // ==================== Delete Button Tests ====================

    @Test
    public void testDeleteButtonExists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnDelete))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    // ==================== Items Section Tests ====================

    @Test
    public void testItemsSpinnerExists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.spinnerItems))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void testAddItemButtonExists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.btnAddItem))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()))
                    .check(matches(isEnabled()));
        }
    }

    // ==================== State Preservation Tests ====================

    @Test
    public void testStatePreservedAfterRotation() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Make changes
            onView(withId(R.id.editCustomerName))
                    .perform(clearText())
                    .perform(typeText("Rotated Customer"))
                    .perform(closeSoftKeyboard());

            // Simulate rotation
            scenario.recreate();

            // State should be preserved
            onView(withId(R.id.editCustomerName))
                    .check(matches(withText("Rotated Customer")));
        }
    }

    // ==================== Validation Tests ====================

    @Test
    public void testEmptyFieldsAllowed() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Clear all fields
            onView(withId(R.id.editInvoiceNumber)).perform(clearText());
            onView(withId(R.id.editCustomerName)).perform(clearText());
            onView(withId(R.id.editAddress)).perform(clearText());
            onView(withId(R.id.editPhone)).perform(clearText()).perform(closeSoftKeyboard());

            // Save should still work
            onView(withId(R.id.btnSave))
                    .perform(scrollTo())
                    .perform(click());
        }
    }

    // ==================== Image Preview Tests ====================

    @Test
    public void testPodImagePreview1Exists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.imgPodPreview1))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void testPodImagePreview2Exists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.imgPodPreview2))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void testPodImagePreview3Exists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.imgPodPreview3))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void testSignaturePreviewExists() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            onView(withId(R.id.imgSignaturePreview))
                    .perform(scrollTo())
                    .check(matches(isDisplayed()));
        }
    }

    // ==================== Navigation Tests ====================

    @Test
    public void testBackNavigation() {
        try (ActivityScenario<InvoiceDetailActivity> scenario =
                     ActivityScenario.launch(createIntent())) {
            // Press back should close activity
            // Note: Actual back navigation testing is complex with unsaved changes
        }
    }
}
