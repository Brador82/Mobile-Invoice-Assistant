# 🔄 Drag-and-Drop Quick Reference

## ⚡ How to Use

```
1. Long-press invoice card (hold for ~500ms)
2. Drag up or down
3. Release to drop in new position
```

**That's it!** No setup, no configuration needed.

## 🎯 Visual Feedback

| State | Appearance |
|-------|------------|
| Normal | 100% opacity, normal size |
| Dragging | 70% opacity, 105% size |
| Dropped | Smooth return to normal |

## 🔧 Implementation Files

- **ItemMoveCallback.java** - Drag-and-drop handler
- **InvoiceAdapter.java** - Position swapping logic
- **MainActivity.java** - ItemTouchHelper setup

## 💡 Use Cases

✅ Manual route adjustment  
✅ Priority deliveries at top  
✅ Group by geographic area  
✅ Fine-tune optimized routes  
✅ Push problem addresses to end  

## 🎨 Gestures

| Gesture | Action |
|---------|--------|
| 👆 Single tap | Open details |
| ✋ Long press | Start drag |
| ↕️ Drag up/down | Move card |
| 📍 Release | Drop card |

## 🚀 Performance

- **Zero lag** - Instant response
- **Works with 100+ items** - No slowdown
- **No database calls** - In-memory only
- **Smooth animations** - Native Android

## 🔄 Workflow Integration

### Before Route Optimization
```
Add invoices → Drag to group → Optimize
```

### After Route Optimization
```
Optimize → Review → Drag to adjust → Navigate
```

## 🐛 Troubleshooting

| Issue | Solution |
|-------|----------|
| Long-press doesn't work | Hold longer (~500ms) |
| Card snaps back | Drag vertically only |
| Can't drag | Ensure not in detail view |

## 📖 Full Documentation

[docs/DRAG_DROP_REORDERING.md](docs/DRAG_DROP_REORDERING.md)

---

**Status:** ✅ Ready to use now!  
**Complexity:** Simple  
**Learning Curve:** 5 seconds  

**Just long-press and drag!** 🎯
