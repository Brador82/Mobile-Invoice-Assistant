# ⚡ Delivery App Quick Reference

## 🏃 Quick Start
```bash
cd android
.\gradlew clean assembleDebug
adb install app\build\outputs\apk\debug\app-debug.apk
```

## 📐 Architecture Pattern: MVVM

```
View (Activity) → ViewModel → Repository → DAO → Database
       ↑              │
       └──(LiveData)──┘
```

## 🗂️ File Structure

```
delivery/
├── data/
│   ├── entities/Delivery.java        # @Entity with Room
│   ├── dao/DeliveryDao.java          # @Dao with queries
│   ├── database/DeliveryDatabase.java # @Database singleton
│   └── repository/DeliveryRepository.java
├── viewmodel/DeliveryViewModel.java   # AndroidViewModel
├── ui/
│   ├── activities/DeliveryDashboardActivity.java
│   └── adapters/DeliveryAdapter.java  # ListAdapter + DiffUtil
├── utils/
│   ├── RouteOptimizer.java           # 4 algorithms
│   └── DeliveryHelper.java           # Call, navigate, SMS
└── models/
    ├── DeliveryStatus.java           # 6 states
    └── Priority.java                 # 4 levels
```

## 🎨 Key UI Components

```xml
<!-- Dashboard Layout -->
<CoordinatorLayout>
  <AppBarLayout>
    <Toolbar />
    <StatisticsCard />
    <ChipGroup /> <!-- Filters -->
  </AppBarLayout>
  <RecyclerView /> <!-- Deliveries -->
  <ExtendedFloatingActionButton /> <!-- Add new -->
</CoordinatorLayout>
```

## 📊 Data Model

```java
@Entity(tableName = "deliveries")
public class Delivery {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    // Customer
    private String customerName;
    private String customerPhone;
    
    // Address + GPS
    private String streetAddress;
    private Double latitude;
    private Double longitude;
    
    // Delivery Info
    private String packageDescription;
    private int packageCount;
    private DeliveryStatus status;
    private Priority priority;
    
    // Time Management
    private Date scheduledDate;
    private String timeWindowStart; // "09:00"
    private String timeWindowEnd;   // "12:00"
    
    // Route
    private int routeOrder;
    private Date estimatedArrival;
    
    // Proof of Delivery
    private String signaturePath;
    private String podPhotoPaths;
}
```

## 🔄 Status Lifecycle

```
PENDING → IN_TRANSIT → DELIVERED ✅
              ↓
            FAILED → RESCHEDULED
              ↓
          CANCELLED
```

## 💾 Common Database Operations

```java
// In DAO
@Query("SELECT * FROM deliveries ORDER BY route_order ASC")
LiveData<List<Delivery>> getAllDeliveries();

@Query("SELECT * FROM deliveries WHERE status = :status")
LiveData<List<Delivery>> getDeliveriesByStatus(DeliveryStatus status);

@Query("SELECT * FROM deliveries WHERE status IN ('PENDING', 'IN_TRANSIT')")
LiveData<List<Delivery>> getActiveDeliveries();

@Insert(onConflict = OnConflictStrategy.REPLACE)
long insert(Delivery delivery);

@Update
int update(Delivery delivery);

@Delete
int delete(Delivery delivery);
```

## 🎯 ViewModel Usage

```java
// In Activity onCreate()
viewModel = new ViewModelProvider(this).get(DeliveryViewModel.class);

// Observe data
viewModel.getAllDeliveries().observe(this, deliveries -> {
    adapter.submitList(deliveries);
});

// Filter by status
viewModel.setStatusFilter(DeliveryStatus.PENDING);

// Search
viewModel.setSearchQuery("John");

// Insert new delivery
Delivery delivery = new Delivery();
delivery.setCustomerName("John Doe");
viewModel.insert(delivery);

// Update status
viewModel.updateStatus(deliveryId, DeliveryStatus.DELIVERED);
```

## 🗺️ Route Optimization

```java
// Nearest Neighbor (distance-based)
List<Delivery> optimized = RouteOptimizer.optimizeByNearestNeighbor(
    deliveries, startLat, startLng
);

// Time Window Priority (schedule-based)
List<Delivery> optimized = RouteOptimizer.optimizeByTimeWindow(deliveries);

// Priority First (urgent first)
List<Delivery> optimized = RouteOptimizer.optimizeByPriority(deliveries);

// Smart Hybrid (recommended)
List<Delivery> optimized = RouteOptimizer.optimizeSmart(
    deliveries, startLat, startLng
);

// Calculate distance
double km = RouteOptimizer.calculateDistance(lat1, lng1, lat2, lng2);

// Estimate time
int minutes = RouteOptimizer.estimateDeliveryTimeMinutes(distanceKm);
```

## 📞 Helper Functions

```java
// Call customer
DeliveryHelper.callCustomer(context, delivery);

// Navigate to address
DeliveryHelper.navigateToDelivery(context, delivery);

// Send SMS
DeliveryHelper.sendSMS(context, delivery, "Your delivery is on the way!");

// Format phone
String formatted = DeliveryHelper.formatPhoneNumber("5551234567");
// Returns: (555) 123-4567

// Get ETA string
String eta = DeliveryHelper.getETAString(delivery);
// Returns: "45 min" or "2h 15m"
```

## 🎨 Color Codes

```java
// Status Colors
PENDING:      "#FFA726" (Orange)
IN_TRANSIT:   "#42A5F5" (Blue)
DELIVERED:    "#66BB6A" (Green)
FAILED:       "#EF5350" (Red)
RESCHEDULED:  "#AB47BC" (Purple)
CANCELLED:    "#78909C" (Grey)

// Priority Colors
LOW:      "#4CAF50" (Green)
NORMAL:   "#2196F3" (Blue)
HIGH:     "#FF9800" (Orange)
URGENT:   "#F44336" (Red)
```

## 🔍 RecyclerView Adapter

```java
// Modern ListAdapter with DiffUtil
public class DeliveryAdapter extends ListAdapter<Delivery, ViewHolder> {
    // Constructor
    public DeliveryAdapter() {
        super(DIFF_CALLBACK);
    }
    
    // Submit new list (DiffUtil calculates changes)
    adapter.submitList(newDeliveries);
    
    // Click listener
    adapter.setOnDeliveryClickListener(delivery -> {
        // Handle click
    });
}
```

## 🎭 Swipe Actions

```java
ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(
    0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT
) {
    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        if (direction == ItemTouchHelper.RIGHT) {
            // Complete delivery
            viewModel.updateStatus(delivery.getId(), DeliveryStatus.DELIVERED);
        } else {
            // Delete delivery
            viewModel.delete(delivery);
        }
    }
};
new ItemTouchHelper(swipeCallback).attachToRecyclerView(recyclerView);
```

## 📊 Statistics Queries

```java
// Get counts
LiveData<Integer> totalCount = viewModel.getTotalCount();
LiveData<Integer> pendingCount = viewModel.getCountByStatus(DeliveryStatus.PENDING);
LiveData<Integer> todaysCount = viewModel.getTodaysCount();
LiveData<Integer> overdueCount = viewModel.getOverdueCount();

// Observe in UI
totalCount.observe(this, count -> {
    tvTotalCount.setText(String.valueOf(count));
});
```

## 🛠️ Gradle Commands

```bash
# Clean build
.\gradlew clean

# Build debug APK
.\gradlew assembleDebug

# Install on device
.\gradlew installDebug

# Run tests
.\gradlew test

# List tasks
.\gradlew tasks
```

## 📝 Key Dependencies

```gradle
// Lifecycle (MVVM)
implementation "androidx.lifecycle:lifecycle-viewmodel:2.7.0"
implementation "androidx.lifecycle:lifecycle-livedata:2.7.0"

// Room Database
implementation "androidx.room:room-runtime:2.6.0"
annotationProcessor "androidx.room:room-compiler:2.6.0"

// Material Design 3
implementation 'com.google.android.material:material:1.11.0'

// RecyclerView
implementation 'androidx.recyclerview:recyclerview:1.3.2'
```

## 🧪 Testing Checklist

- [ ] Create delivery
- [ ] View list
- [ ] Filter by status
- [ ] Search deliveries
- [ ] Swipe to complete
- [ ] Swipe to delete
- [ ] Tap for details
- [ ] Call customer
- [ ] Navigate to address
- [ ] Optimize route
- [ ] View statistics
- [ ] Export data

## 🚦 Status Checks

```java
// Check status
if (delivery.getStatus().isCompleted()) {
    // DELIVERED or CANCELLED
}

if (delivery.getStatus().isActive()) {
    // PENDING or IN_TRANSIT
}

// Check priority
if (delivery.getPriority().getValue() >= 3) {
    // HIGH or URGENT
}

// Check time
if (delivery.isOverdue()) {
    // Past scheduled date
}

// Check location
if (delivery.hasCoordinates()) {
    // Has GPS data
}
```

## 📱 UI State Management

```java
// Loading state
viewModel.isLoading().observe(this, isLoading -> {
    progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
});

// Error handling
viewModel.getErrorMessage().observe(this, error -> {
    if (error != null) {
        Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        viewModel.clearError();
    }
});
```

## 🎯 Best Practices

✅ **Always observe LiveData** in `onCreate()`  
✅ **Use ViewBinding** for type-safe view access  
✅ **Let Repository handle threading** - don't block main thread  
✅ **Use DiffUtil** for list updates - much faster  
✅ **Keep Activities thin** - logic goes in ViewModel  
✅ **Use Type Converters** for complex types in Room  
✅ **Add JavaDoc comments** for public methods  
✅ **Handle null safely** - check before using  

## 🚀 Performance Tips

⚡ **DiffUtil** instead of notifyDataSetChanged()  
⚡ **ViewBinding** instead of findViewById()  
⚡ **LiveData** instead of manual updates  
⚡ **Background threads** for database operations  
⚡ **Singleton Database** - one instance only  
⚡ **Index frequently queried columns** in Room  

## 📚 Documentation

- **DELIVERY_APP_ARCHITECTURE.md** - Full architecture
- **BUILD_GUIDE_DELIVERY_APP.md** - Build instructions  
- **DESIGN_COMPARISON.md** - Old vs New comparison
- **Quick Reference** - This file

---

**Keep this handy for quick lookups! 📌**
