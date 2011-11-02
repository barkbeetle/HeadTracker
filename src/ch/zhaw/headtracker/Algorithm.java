package ch.zhaw.headtracker;

import ch.zhaw.headtracker.image.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;

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
					Image background = ImageUtil.scaleDown(grabber.getImage(), 2);

					while (true) {
						view.update(algorithm(background, ImageUtil.scaleDown(grabber.getImage(), 2)));
						
						Thread.sleep(100);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});

		thread.setDaemon(true);
		thread.start();
	}

	private static ImageView.Painter algorithm(Image background, final Image image) {
		
		final Image mask = new Image(image.width, image.height);
		
		for(int y = 0; y < image.height; y += 1) {
			for(int x = 0; x < image.width; x += 1) {
				int bgPixel = background.getPixel(x, y);
				int imgPixel = image.getPixel(x, y);
				
				mask.setPixel(x, y, Math.abs(bgPixel - imgPixel) < filterThreshold);
			}
		}

		ImageUtil.maximum(mask, 5);
		ImageUtil.minimum(mask, 5);
		
		ImageUtil.bitOr(image, mask);
		
		return new ImageView.Painter(mask) {
			@Override
			public void draw(Graphics2D g2) {
				g2.setPaint(Color.red);
			//	g2.draw(new Rectangle2D.Double(10, 10, image.width - 20, image.height - 20));
		
				for (int ix = 0; ix < mask.width; ix += 20) {
					for (int iy = 0; iy < mask.height; iy += 1) {
						if (mask.getPixel(ix, iy) < 0xff) {
							g2.draw(new Line2D.Double(ix, 0, ix, iy));
							g2.draw(new Line2D.Double(ix, iy, ix - 8, iy - 8));
							g2.draw(new Line2D.Double(ix, iy, ix + 8, iy - 8));
							
							break;
						}
					}
				}
			}
		};
	}
}
