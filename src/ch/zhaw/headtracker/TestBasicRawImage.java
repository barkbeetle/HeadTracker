package ch.zhaw.headtracker;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class TestBasicRawImage {
	private TestBasicRawImage() {
	}

	public static void main(String[] args) {
		try {
			test();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	@SuppressWarnings({ "SocketOpenedButNotSafelyClosed" })
	private static void test() throws IOException {
		Socket socket = new Socket(InetAddress.getByName("10.0.0.3"), 9999);
		
		final TestRawImage panel = new TestRawImage(752, 480);
		final InputStream input = socket.getInputStream();
		
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true)
						panel.setImage(loadImage(input, 752, 480));
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	private static BufferedImage loadImage(InputStream input, int width, int height) throws IOException {
		BufferedImage image = new BufferedImage(752, 480, BufferedImage.TYPE_INT_BGR);

		for (int iy = 0; iy < height; iy += 1) {
			for (int ix = 0; ix < width; ix += 1) {
				int pixel = input.read();

				if (pixel == -1)
					throw new RuntimeException();
				
				image.setRGB(ix, iy, pixel << 16 | pixel << 8 | pixel);
			}
		}

		return image;
	}
}
