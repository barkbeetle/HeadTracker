package ch.zhaw.headtracker;

import ch.zhaw.headtracker.gui.ControlPanel;
import ch.zhaw.headtracker.image.*;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class Algorithm {
	private Algorithm() {
	}

	public static int filterThreshold = 10;
	public static int minimum = 10;
	public static int maximum = 10;
	public static int contrastThreshold = 50;
	public static boolean showOriginal = false;

	public static void run(final ImageGrabber grabber) {
		final ImageView view = new ImageView(752, 480);
		final ControlPanel controlPanel = new ControlPanel();

		// align control panel to image view
		controlPanel.setLocation(view.getLocation().x + view.getSize().width + 10, view.getLocation().y);

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

		if (showOriginal) {
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

					mask.setPixel(x, y, Math.abs(bgPixel - imgPixel) < filterThreshold);
				}
			}
			
			ImageUtil.minimum(mask, 10);
			ImageUtil.maximum(mask, 10);
			
			ImageUtil.bitOr(mask, image);
			ImageUtil.threshold(mask, contrastThreshold);
			
			mask.invert();

		//	final Image minImage = opening(mask, minimum);
		//	Image maxImage = opening(mask, maximum);
			
		//	maxImage.invert();
			
		//	ImageUtil.bitOr(minImage, maxImage);

		//	minImage.invert();

			return new ImageView.Painter(mask) {
				@Override
				public void draw(Graphics2D g2) {
					g2.setPaint(Color.red);

					List<Segmentation.object> objects = new Segmentation().findObjects(mask, 127);

					for (Segmentation.object i : objects) {
						g2.draw(new Rectangle2D.Double(i.left, i.top, i.right - i.left, i.bottom - i.top));
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
