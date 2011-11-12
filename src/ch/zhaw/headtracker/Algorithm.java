package ch.zhaw.headtracker;

import ch.zhaw.headtracker.gui.ControlPanel2;
import ch.zhaw.headtracker.image.*;
import ch.zhaw.headtracker.image.Image;
import java.awt.*;
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
	private static final SliderSetting contrastThreshold = new SliderSetting("Contrast threshold", 0, 255, 50);
	private static final CheckBoxSetting showOriginal = new CheckBoxSetting("Show original", false);

	public static void run(final ImageGrabber grabber) {
		final ImageView view = new ImageView(752, 480);
		ControlPanel2 controlPanel = new ControlPanel2(filterThreshold, minimum, maximum, contrastThreshold, showOriginal);

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
			
			ImageUtil.minimum(mask, 10);
			ImageUtil.maximum(mask, 10);
			
			ImageUtil.bitOr(mask, image);
			ImageUtil.threshold(mask, contrastThreshold.value);
			
		//	mask.invert();

		//	final Image minImage = opening(mask, minimum);
		//	Image maxImage = opening(mask, maximum);
			
		//	maxImage.invert();
			
		//	ImageUtil.bitOr(minImage, maxImage);

		//	minImage.invert();

			return new ImageView.Painter(mask) {
				@Override
				public void draw(Graphics2D g2) {
					g2.setPaint(Color.red);

					Set<Segmentation.Group> groups = Segmentation.findGroups(mask);

					for (Segmentation.Group i : groups) {
						g2.draw(new Rectangle2D.Double(i.left - .5, i.top - .5, i.right - i.left + .5, i.bottom - i.top + .5));
						
						g2.drawString(String.format("%d", i.sum), i.left, i.top);
					}

					//	g2.draw(new Rectangle2D.Double(10, 10, image.width - 20, image.height - 20));

					//for (int ix = 0; ix < mask.width; ix += 20) {
					//	for (int iy = 0; iy < mask.height; iy += 1) {
					//		if (mask.getPixel(ix, iy) < 0xff) {
					//			g2.draw(new Line2D.Double(ix, 0, ix, iy));
					//			g2.draw(new Line2D.Double(ix, iy, ix - 8, iy - 8));
					//			g2.draw(new Line2D.Double(ix, iy, ix + 8, iy - 8));
					//
					//			break;
					//		}
					//	}
					//}
				}
			};
		}
	}
	
	//public static List<Spot> findSpots(Image image, int minSize, int maxSize) {
	//	class Group {
	//		int size;
	//		int posSumX;
	//		int posSumY;
	//
	//		Group(int posSumX, int posSumY, int size) {
	//			this.posSumX = posSumX;
	//			this.posSumY = posSumY;
	//			this.size = size;
	//		}
	//	}
	//	
	//	class Range {
	//		int start;
	//		int end;
	//		Group group;
	//	}
	//
	//	List<Range> rangesLast = new ArrayList<Range>(); // ranges on the last line
	//	List<Range> rangesCurrent = new ArrayList<Range>(); // ranges on the current range
	//	for (int iy = 0; iy < image.height; iy += 1) {
	//		Range currentRange = null;
	//		
	//		for (int ix = 0; ix < image.width; ix += 1) {
	//			if (image.getPixel(iy, ix) == 0) {
	//				if (currentRange == null) {
	//					currentRange = new Range();
	//
	//					currentRange.start = iy;
	//				}
	//			} else {
	//				if (currentRange != null) {
	//					currentRange.end = ix;
	//
	//					rangesCurrent.add(currentRange);
	//				}
	//			}
	//		}
	//		
	//		if (currentRange != null) {
	//			currentRange.end = image.width;
	//
	//			rangesCurrent.add(currentRange);
	//		}
	//		
	//		int rangesLastPos = 0;
	//		int rangesCurrentPos = 0;
	//		
	//		while (rangesLastPos < rangesLast.size() || rangesCurrentPos < rangesCurrent.size()) {
	//			if (rangesLast.get(rangesLastPos).start < rangesCurrent.get(rangesCurrentPos).start) {
	//				if (rangesLast.get(rangesLastPos).end < rangesCurrent.get(rangesCurrentPos).start) {
	//					
	//				}
	//			}
	//		}
	//	}
	//	
	//	return null;
	//}
	//
	//public static final class Spot {
	//	public final int posX;
	//	public final int posY;
	//	public final int size;
	//
	//	public Spot(int posX, int posY, int size) {
	//		this.posX = posX;
	//		this.posY = posY;
	//		this.size = size;
	//	}
	//}
}
