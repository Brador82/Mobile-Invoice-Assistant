# 🚀 Quick Start Guide

## For Windows Users

### 1. Install Dependencies

```powershell
# Install Python packages
pip install -r requirements.txt
```

### 2. Install Tesseract OCR

Download and install from: <https://github.com/UB-Mannheim/tesseract/wiki>

Default installation path: `C:\Program Files\Tesseract-OCR`

### 3. Start the Server

```powershell
python Server.py
```

You should see startup logs in the terminal.

### 4. Open in Browser

#### On Computer

- Navigate to: <http://localhost:5000>

#### On Mobile Device

1. Find your computer's IP address:

   ```powershell
   ipconfig
   ```

   Look for "IPv4 Address" (e.g., 192.168.1.100)

1. On your mobile device, open browser and go to:

   ```text
   http://YOUR_IP_ADDRESS:5000
   ```

   (Replace YOUR_IP_ADDRESS with actual IP)

### 5. Test the App

1. Click "📁 Upload Multiple Files" and select an invoice image
2. Click "🧠 Process All Images with OCR"
3. Watch as data is automatically extracted!
4. Try the signature pad, POD capture, and export features

## Troubleshooting

### "Tesseract not found" Error

Add Tesseract to your system PATH:

1. Right-click "This PC" → Properties
2. Advanced system settings → Environment Variables
3. Under "System variables", find "Path"
4. Click "Edit" → "New"
5. Add: `C:\Program Files\Tesseract-OCR`
6. Click OK and restart terminal

### Camera Not Working

- Ensure you're using **HTTPS** or **localhost**
- Grant camera permissions when prompted
- Try a different browser (Chrome recommended)
- Use file upload as alternative

### Port 5000 Already in Use

Change the port in `Server.py`:

```python
app.run(host="0.0.0.0", port=8080, debug=False)
```

Then access via: <http://localhost:8080>

## Tips for Best Results

### Invoice Photos

- ✅ Good lighting (no shadows)
- ✅ Flat surface (no wrinkles)
- ✅ Clear focus
- ✅ Fill the frame with invoice
- ❌ Avoid glare or reflections
- ❌ Don't tilt or angle the photo

### Signature Capture

- Use landscape orientation for more space
- Draw slowly and clearly
- Click "Clear" if you make a mistake
- Click "Save" when satisfied

### Daily Workflow

1. Morning: Open app on mobile device
2. At each delivery: Capture invoice → Process OCR
3. Get customer signature
4. Take POD photo
5. Add notes
6. Evening: Export data and share/backup
7. Click "Clear Log" to start fresh next day

## Need Help?

- Check the full README.md for detailed documentation
- View browser console (F12) for error messages
- Check Python server output for backend errors
- Ensure all dependencies are properly installed
