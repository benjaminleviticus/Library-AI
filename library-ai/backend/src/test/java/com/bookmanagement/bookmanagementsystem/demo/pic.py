from PIL import Image

# Create a blank white 16x16 image
img = Image.new('RGB', (16, 16), color='white')

# Save as favicon.ico
img.save('favicon.ico')

print("favicon.ico created successfully.")