#!/usr/bin/env python3
"""
Generate app icon for Mobile Invoice Assistant
Creates a modern, professional icon with delivery + invoice theme
"""

from PIL import Image, ImageDraw, ImageFont
import os

def create_icon(size, output_path):
    """Create app icon with specified size"""
    
    # Create image with transparency
    img = Image.new('RGBA', (size, size), (0, 0, 0, 0))
    draw = ImageDraw.Draw(img)
    
    # Color scheme
    primary_blue = (33, 150, 243)  # #2196F3
    accent_orange = (255, 152, 0)  # #FF9800
    white = (255, 255, 255)
    dark_blue = (21, 101, 192)  # Darker blue for depth
    
    # Add gradient background (rounded square)
    margin = int(size * 0.08)  # 8% margin
    corner_radius = int(size * 0.22)  # 22% corner radius for modern look
    
    # Create rounded rectangle background with gradient effect
    for i in range(margin, size - margin):
        # Calculate gradient color
        ratio = (i - margin) / (size - 2 * margin)
        r = int(dark_blue[0] + (primary_blue[0] - dark_blue[0]) * ratio)
        g = int(dark_blue[1] + (primary_blue[1] - dark_blue[1]) * ratio)
        b = int(dark_blue[2] + (primary_blue[2] - dark_blue[2]) * ratio)
        color = (r, g, b, 255)
        
        draw.rectangle([margin, i, size - margin, i + 1], fill=color)
    
    # Clip to rounded rectangle
    mask = Image.new('L', (size, size), 0)
    mask_draw = ImageDraw.Draw(mask)
    mask_draw.rounded_rectangle(
        [margin, margin, size - margin, size - margin],
        radius=corner_radius,
        fill=255
    )
    
    # Apply mask
    img.putalpha(mask)
    
    # Re-draw on top for icon elements
    draw = ImageDraw.Draw(img)
    
    # Draw document/invoice icon (white)
    doc_width = int(size * 0.45)
    doc_height = int(size * 0.55)
    doc_x = int(size * 0.28)
    doc_y = int(size * 0.22)
    
    # Document shape with folded corner
    fold_size = int(doc_width * 0.25)
    doc_points = [
        (doc_x, doc_y),
        (doc_x + doc_width - fold_size, doc_y),
        (doc_x + doc_width, doc_y + fold_size),
        (doc_x + doc_width, doc_y + doc_height),
        (doc_x, doc_y + doc_height)
    ]
    draw.polygon(doc_points, fill=white)
    
    # Folded corner
    corner_points = [
        (doc_x + doc_width - fold_size, doc_y),
        (doc_x + doc_width, doc_y + fold_size),
        (doc_x + doc_width - fold_size, doc_y + fold_size)
    ]
    draw.polygon(corner_points, fill=(200, 200, 200))
    
    # Document lines (text representation)
    line_margin = int(doc_width * 0.15)
    line_y_start = doc_y + int(doc_height * 0.25)
    line_spacing = int(doc_height * 0.12)
    line_width = int(doc_width * 0.7)
    
    for i in range(4):
        y = line_y_start + (i * line_spacing)
        width = line_width if i < 3 else int(line_width * 0.6)
        draw.rectangle(
            [doc_x + line_margin, y, doc_x + line_margin + width, y + 2],
            fill=primary_blue
        )
    
    # Draw scanning effect (orange accent)
    scan_line_count = 3
    scan_y_start = doc_y + int(doc_height * 0.4)
    scan_spacing = int(doc_height * 0.08)
    
    for i in range(scan_line_count):
        y = scan_y_start + (i * scan_spacing)
        # Gradient scan lines
        alpha = int(200 - (i * 40))
        draw.line(
            [(doc_x - 5, y), (doc_x + doc_width + 5, y)],
            fill=accent_orange + (alpha,),
            width=2
        )
    
    # Add small delivery truck icon in corner (bottom right)
    truck_size = int(size * 0.18)
    truck_x = size - margin - truck_size - int(size * 0.08)
    truck_y = size - margin - truck_size - int(size * 0.08)
    
    # Simple truck shape
    # Cab
    cab_width = int(truck_size * 0.35)
    cab_height = int(truck_size * 0.6)
    draw.rounded_rectangle(
        [truck_x, truck_y + truck_size - cab_height, 
         truck_x + cab_width, truck_y + truck_size],
        radius=2,
        fill=accent_orange
    )
    
    # Cargo area
    cargo_width = int(truck_size * 0.65)
    cargo_height = int(truck_size * 0.75)
    draw.rounded_rectangle(
        [truck_x + cab_width - 2, truck_y + truck_size - cargo_height,
         truck_x + cab_width + cargo_width, truck_y + truck_size],
        radius=2,
        fill=accent_orange
    )
    
    # Wheels
    wheel_radius = int(truck_size * 0.12)
    wheel_y = truck_y + truck_size - wheel_radius // 2
    # Front wheel
    draw.ellipse(
        [truck_x + cab_width // 2 - wheel_radius, wheel_y - wheel_radius,
         truck_x + cab_width // 2 + wheel_radius, wheel_y + wheel_radius],
        fill=dark_blue
    )
    # Back wheel
    draw.ellipse(
        [truck_x + cab_width + cargo_width - wheel_radius * 2, wheel_y - wheel_radius,
         truck_x + cab_width + cargo_width, wheel_y + wheel_radius],
        fill=dark_blue
    )
    
    # Save
    img.save(output_path, 'PNG')
    print(f"Created icon: {output_path} ({size}x{size})")

def main():
    """Generate all required icon sizes"""
    
    # Icon sizes for Android
    sizes = {
        'mipmap-mdpi': 48,
        'mipmap-hdpi': 72,
        'mipmap-xhdpi': 96,
        'mipmap-xxhdpi': 144,
        'mipmap-xxxhdpi': 192
    }
    
    base_path = os.path.join(os.path.dirname(__file__), '..', 'android', 'app', 'src', 'main', 'res')
    
    for folder, size in sizes.items():
        folder_path = os.path.join(base_path, folder)
        os.makedirs(folder_path, exist_ok=True)
        
        # Create launcher icon
        icon_path = os.path.join(folder_path, 'ic_launcher.png')
        create_icon(size, icon_path)
        
        # Create round icon
        round_icon_path = os.path.join(folder_path, 'ic_launcher_round.png')
        create_icon(size, round_icon_path)
    
    # Create a large version for Play Store (512x512)
    play_store_path = os.path.join(os.path.dirname(__file__), '..', 'play_store_icon.png')
    create_icon(512, play_store_path)
    
    print("\n✅ All app icons generated successfully!")
    print(f"📱 Android icons: {base_path}")
    print(f"🏪 Play Store icon: {play_store_path}")

if __name__ == '__main__':
    main()
