package ch.zhaw.headtracker;

import ch.zhaw.headtracker.animation.PictureShop;
import ch.zhaw.headtracker.gui.ControlPanel2;
import ch.zhaw.headtracker.image.Image;
import ch.zhaw.headtracker.image.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.Set;

import static ch.zhaw.headtracker.gui.ControlPanel2.*;

public final class Algorithm {
	private final ImageGrabber grabber;
	private final PictureShop pictureShop = new PictureShop("res/3d/jack/out/jack%04d.png", 0, 40);
	private final ImageView view = new ImageView(752, 480);
	private final ControlPanel2 controlPanel = new ControlPanel2();
	private final DropdownMenuSetting showImage = controlPanel.dropdownMenuSetting("Show image", new String[]{ "Original", "Mask", "Black / White" }, 2);
	private final SliderSetting filterThreshold = controlPanel.sliderSetting("Background threshold", 0, 50, 10);
	private final SliderSetting opening = controlPanel.sliderSetting("Opening", 0, 50, 10);
//	private final SliderSetting maximum = controlPanel.sliderSetting("Maximum", 0, 50, 10);
	private final SliderSetting contrastThreshold = controlPanel.sliderSetting("Contrast threshold", 0, 255, 100);
//	private final CheckBoxSetting showPlummets = controlPanel.checkBoxSetting("Show plummets", true);
	private final CheckBoxSetting showAnimation = controlPanel.checkBoxSetting("Show animation", false);
	private final CheckBoxSetting showSegmentation = controlPanel.checkBoxSetting("Show segmentation", true);
	private final ButtonSetting resetBackground = controlPanel.buttonSetting("Reset Background");

	public Algorithm(ImageGrabber grabber) {
		this.grabber = grabber;
	}

	public void run() {
		view.show(new Point(80, 100));
		controlPanel.show(new Point(752 + 10 + 80, 100));

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					grabber.getImage(); // Sometimes the camera fails to set the exposure time correctly for the first image
					
					Image background = ImageUtil.scaleDown(grabber.getImage(), 2);

					while (true) {
						Image image = ImageUtil.scaleDown(grabber.getImage(), 2);
						
						if (resetBackground.getSignal())
							background = new Image(image);
						
						view.update(algorithm(background, image));

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

	private ImageView.Painter algorithm(Image background, final Image image) {
		final Image mask = new Image(image.width, image.height);

		for(int y = 0; y < image.height; y += 1) {
			for(int x = 0; x < image.width; x += 1) {
				int bgPixel = background.getPixel(x, y);
				int imgPixel = image.getPixel(x, y);

				mask.setPixel(x, y, Math.abs(bgPixel - imgPixel) < filterThreshold.value);
			}
		}

		ImageUtil.minimum(mask, opening.value);
		ImageUtil.maximum(mask, opening.value);

		Image maskedImage = new Image(image);
		ImageUtil.bitOr(maskedImage, mask);
		ImageUtil.threshold(maskedImage, contrastThreshold.value);

		final Set<Segmentation.Group> groups = Segmentation.findGroups(image);

		Image shownImage;

		if (showImage.value == 0)
			shownImage = image;
		else if (showImage.value == 1)
			shownImage = mask;
		else
			shownImage = maskedImage;

		return new ImageView.Painter(shownImage) {
			@Override
			public void draw(Graphics2D g2) {
				g2.setPaint(Color.red);
				if (showAnimation.value)
				{
					g2.drawImage(pictureShop.getImageForAngle(0f), 0, 0, 752 / 2, 480 / 2, null);
				}
				
				if (showSegmentation.value) {
					for (Segmentation.Group i : groups) {
						g2.draw(new Rectangle2D.Double(i.left - .5, i.top - .5, i.right - i.left + .5, i.bottom - i.top + .5));
						
						g2.drawString(String.format("%d", i.sum), i.left, i.top);
					}
				}

				/*int centerX = mask.width/2;
				int centerY = mask.height/2;
				int left = distanceLeft(mask, centerX, centerY);
				int right = distanceRight(mask, centerX, centerY);
				
				g2.setPaint(Color.blue);
				g2.draw(new Line2D.Double(centerX, centerY, centerX - left, centerY));
				g2.draw(new Line2D.Double(centerX - left, centerY, centerX - left + 4, centerY - 4));
				g2.draw(new Line2D.Double(centerX - left, centerY, centerX - left + 4, centerY + 4));                   g2.draw(new Line2D.Double(centerX, centerY, centerX + right, centerY));
				g2.draw(new Line2D.Double(centerX + right, centerY, centerX + right - 4, centerY - 4));
				g2.draw(new Line2D.Double(centerX + right, centerY, centerX + right - 4, centerY + 4));*/
			}
		};
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
