package ch.zhaw.headtracker;

import ch.zhaw.headtracker.gui.ControlPanel2;
import ch.zhaw.headtracker.image.Image;
import ch.zhaw.headtracker.image.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import static ch.zhaw.headtracker.gui.ControlPanel2.CheckBoxSetting;
import static ch.zhaw.headtracker.gui.ControlPanel2.SliderSetting;

public class Algorithm {
	private Algorithm() {
	}

	private static final SliderSetting filterThreshold = new SliderSetting("Background threshold", 0, 50, 10);
	private static final SliderSetting minimum = new SliderSetting("Minimum", 0, 50, 10);
	private static final SliderSetting maximum = new SliderSetting("Maximum", 0, 50, 10);
	private static final SliderSetting contrastThreshold = new SliderSetting("Contrast threshold", 0, 255, 100);
	private static final CheckBoxSetting showOriginal = new CheckBoxSetting("Show original", false);
	private static final CheckBoxSetting showPlummets = new CheckBoxSetting("Show plummets", true);
	private static final CheckBoxSetting showSegmentation = new CheckBoxSetting("Show segmentation", true);

	public static void run(final ImageGrabber grabber) {
		final ImageView view = new ImageView(752, 480);
		ControlPanel2 controlPanel = new ControlPanel2(filterThreshold, minimum, maximum, contrastThreshold, showOriginal, showPlummets, showSegmentation);

		view.show(new Point(80, 100));
		controlPanel.show(new Point(752 + 10 + 80, 100));

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					grabber.getImage(); // Sometimes the camera fails to set the exposure time correctly for the first image
					
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

	// make black spots smaller than radius vanish
	private static Image opening(Image image, int radius) {
		Image res = new Image(image);
		
		ImageUtil.maximum(res, radius);
		ImageUtil.minimum(res, radius);
		
		return res;
	}

	// make white spots smaller than radius vanish
	private static Image closing(Image image, int radius) {
		Image res = new Image(image);

		ImageUtil.maximum(res, radius);
		ImageUtil.minimum(res, radius);

		return res;
	}

	private static ImageView.Painter algorithm(Image background, final Image image) {

		if (showOriginal.value) {
			return new ImageView.Painter(image) {
				@Override
				protected void draw(Graphics2D g2) {
				}
			};
		} else {
			final Image mask = new Image(image.width, image.height);

			for(int y = 0; y < image.height; y += 1) {
				for(int x = 0; x < image.width; x += 1) {
					int bgPixel = background.getPixel(x, y);
					int imgPixel = image.getPixel(x, y);

					mask.setPixel(x, y, Math.abs(bgPixel - imgPixel) < filterThreshold.value);
				}
			}

			ImageUtil.minimum(mask, minimum.value);
			ImageUtil.maximum(mask, maximum.value);

			ImageUtil.bitOr(image, mask);
			ImageUtil.threshold(image, contrastThreshold.value);
			
			return new ImageView.Painter(image) {
				@Override
				public void draw(Graphics2D g2) {
					g2.setPaint(Color.red);
					
					if (showSegmentation.value) {
						Set<Segmentation.Group> groups = Segmentation.findGroups(image);
						
						for (Segmentation.Group i : groups) {
							g2.draw(new Rectangle2D.Double(i.left - .5, i.top - .5, i.right - i.left + .5, i.bottom - i.top + .5));
							
							g2.drawString(String.format("%d", i.sum), i.left, i.top);
						}
					}
					
					if (showPlummets) {
						g2.setPaint(Color.red);
						for (int ix = 0; ix < mask.width; ix += 20) {
							for (int iy = 0; iy < mask.height; iy += 1) {
								if (mask.getPixel(ix, iy) < 0xff) {
									g2.draw(new Line2D.Double(ix, 0, ix, iy));
									g2.draw(new Line2D.Double(ix, iy, ix - 4, iy - 4));
									g2.draw(new Line2D.Double(ix, iy, ix + 4, iy - 4));
	
									break;
								}
							}
						}
					}
				}
			};
		}
	}

	private static int distanceLeft(Image mask, int x, int y) {
		int distance = 0;
		while(x >= 0) {
			if(mask.getPixel(x, y) > 0)
				return distance;
			distance++;
			x--;
		}
		return 0;
	}

	private static int distanceRight(Image mask, int x, int y) {
		int distance = 0;
		while(x < mask.width) {
			if(mask.getPixel(x, y) > 0)
				return distance;
			distance++;
			x++;
		}
		return 0;
	}
}
