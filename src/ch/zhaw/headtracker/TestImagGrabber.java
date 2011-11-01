package ch.zhaw.headtracker;

import java.awt.Graphics2D;
import java.io.IOException;

public class TestImagGrabber {
	public static final int FILTER_THRESHOLD = 10;

	private TestImagGrabber() {
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
		final ImageGrabber grabber = RawInputStreamImageGrabber.fromSocketAddress("10.0.0.3", (short) 9999, 40000, 752, 480);
		final ImageView view = new ImageView(752, 480);
		
		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {



					Image background = grabber.getImage().shrink(2);


					while (true) {

						Image image = grabber.getImage().shrink(2);

						for(int y=0; y<image.height; y++) {
							for(int x=0; x<image.width; x++) {
								int bgPixel = background.getPixel(x, y);
								int imgPixel = image.getPixel(x, y);

								if(Math.abs(bgPixel - imgPixel) < FILTER_THRESHOLD)
									image.setPixel(x, y, 255);
							}
						}

						view.update(new ImageView.Painter(image) {
							@Override
							public void draw(Graphics2D graphics) {
							}
						});
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
		
		thread.setDaemon(true);
		thread.start();
	}
}
