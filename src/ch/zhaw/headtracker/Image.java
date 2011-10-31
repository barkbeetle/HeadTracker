package ch.zhaw.headtracker;

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
	
	public byte getPixel(int x, int y) {
		assert 0 <= x && x < width && 0 <= y && y < height;
		
		return pixels[width * y + x];
	}
	
	public void setPixel(int x, int y, byte value) {
		assert 0 <= x && x < width && 0 <= y && y < height;

		pixels[width * y + x] = value;
	}
	
	public byte[] getData() {
		return Arrays.copyOf(pixels, pixels.length);
	}
	
	public static Image readFromStream(InputStream input, int width, int height) throws IOException {
		int pos = 0;
		byte[] pixels = new byte[width * height];
		
		while (pos < width * height)
			pos += input.read(pixels, pos, width * height - pos);
		
		return new Image(pixels, width, height);
	}
}
