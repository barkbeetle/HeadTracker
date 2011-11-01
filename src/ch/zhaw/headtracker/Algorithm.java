package ch.zhaw.headtracker;

import ch.zhaw.headtracker.image.*;
import java.awt.Graphics2D;

public class Algorithm {
	private Algorithm() {
	}

	public static final int filterThreshold = 10;
	
	public static void run(final ImageGrabber grabber) {
		final ImageView view = new ImageView(752, 480);

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Image background = grabber.getImage().shrink(2);

					while (true) {
						Thread.sleep(100);

						Image image = grabber.getImage().shrink(2);

						for(int y=0; y<image.height; y++) {
							for(int x=0; x<image.width; x++) {
								int bgPixel = background.getPixel(x, y);
								int imgPixel = image.getPixel(x, y);

								if(Math.abs(bgPixel - imgPixel) < filterThreshold)
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
