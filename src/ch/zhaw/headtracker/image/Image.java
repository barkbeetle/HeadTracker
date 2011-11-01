package ch.zhaw.headtracker.image;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public final class Image {
	private final byte[] pixels;
	public final int width;
	public final int height;

	private Image(byte[] pixels, int width, int height) {
		this.pixels = pixels;
		this.width = width;
		this.height = height;
	}

	public Image(int width, int height) {
		this(new byte[width * height], width, height);
	}

	public Image(Image image) {
		this(image.getData(), image.width, image.height);
	}

	public int getPixel(int x, int y) {
		assert 0 <= x && x < width && 0 <= y && y < height;

		return pixels[width * y + x] & 0xff;
	}

	public void setPixel(int x, int y, int value) {
		assert 0 <= x && x < width && 0 <= y && y < height;
		assert 0 <= value && value <= 0xff;

		pixels[width * y + x] = (byte) value;
	}

	public void setPixel(int x, int y, boolean value) {
		setPixel(x, y, value ? 0xff : 0);
	}

	public void invert() {
		for (int iy = 0; iy < height; iy += 1)
			for (int ix = 0; ix < width; ix += 1)
				setPixel(ix, iy, ~getPixel(ix, iy) & 0xff);
	}

	// Add pixel values of other to this image's values
	public void add(Image other) {
		assert other.height == height && other.width == width;

		for (int iy = 0; iy < height; iy += 1) {
			for (int ix = 0; ix < width; ix += 1) {
				int pixel = getPixel(ix, iy) + other.getPixel(ix, iy);

				setPixel(ix, iy, (byte) (pixel > 0xff ? 0xff : pixel));
			}
		}
	}

	// Subtract pixel values of other from this image's values
	public void subtract(Image other) {
		assert other.height == height && other.width == width;

		for (int iy = 0; iy < height; iy += 1) {
			for (int ix = 0; ix < width; ix += 1) {
				int pixel = getPixel(ix, iy) - other.getPixel(ix, iy);

				setPixel(ix, iy, (byte) (pixel < 0 ? 0 : pixel));
			}
		}
	}

	public void multiply(Image other) {
		assert other.height == height && other.width == width;

		for(int y = 0; y < height; y += 1)
			for(int x = 0; x < width; x += 1)
				setPixel(x, y, getPixel(x, y) * other.getPixel(x, y) / 0xff);
	}

	public void bitAnd(Image other) {
		assert other.height == height && other.width == width;

		for(int y = 0; y < height; y += 1)
			for(int x = 0; x < width; x += 1)
				setPixel(x, y, getPixel(x, y) & other.getPixel(x, y));
	}

	public void bitOr(Image other) {
		assert other.height == height && other.width == width;

		for(int y = 0; y < height; y += 1)
			for(int x = 0; x < width; x += 1)
				setPixel(x, y, getPixel(x, y) | other.getPixel(x, y));
	}

	// Set pixels below threshold to black and the other pixels to white
	public void threshold(int threshold) {
		for (int iy = 0; iy < height; iy += 1)
			for (int ix = 0; ix < width; ix += 1)
				setPixel(ix, iy, getPixel(ix, iy) < threshold ? 0 : 0xff);
	}

	// Enlarge bright parts by radius
	public void grow(int radius) {
		Image tempImage = new Image(width, height);

		for (int iy = 0; iy < height; iy += 1) {
			for (int ix = 0; ix < width; ix += 1) {
				int pixel = 0;
				int min = Math.max(0, ix - radius);
				int max = Math.min(width, ix + radius + 1);

				for (int ix2 = min; ix2 < max; ix2 += 1)
					pixel = Math.max(pixel, getPixel(ix2, iy));

				tempImage.setPixel(ix, iy, pixel);
			}
		}

		for (int iy = 0; iy < height; iy += 1) {
			for (int ix = 0; ix < width; ix += 1) {
				int pixel = 0;
				int min = Math.max(0, iy - radius);
				int max = Math.min(height, iy + radius + 1);

				for (int iy2 = min; iy2 < max; iy2 += 1)
					pixel = Math.max(pixel, tempImage.getPixel(ix, iy2));

				setPixel(ix, iy, pixel);
			}
		}
	}

	// Return a copy of the image shrinked by factor
	public Image shrink(int factor) {
		Image image = new Image(width / factor, height / factor);

		for (int iy = 0; iy < image.height; iy += 1) {
			for (int ix = 0; ix < image.width; ix += 1) {
				int pixel = 0;

				for (int iy2 = 0; iy2 < factor; iy2 += 1)
					for (int ix2 = 0; ix2 < factor; ix2 += 1)
						pixel += getPixel(ix + ix2, iy + iy2);

				image.setPixel(ix, iy, pixel / (factor * factor));
			}
		}

		return image;
	}

	@SuppressWarnings({ "InstanceVariableUsedBeforeInitialized" })
	public byte[] getData() {
		return Arrays.copyOf(pixels, pixels.length);
	}

	public static Image readFromStream(InputStream input, int width, int height) throws IOException {
		int pos = 0;
		byte[] pixels = new byte[width * height];

		while (pos < width * height) {
			int res = input.read(pixels, pos, width * height - pos);
			
			if (res < 0)
				throw new IOException();
			
			pos += res;
		}

		return new Image(pixels, width, height);
	}
}
