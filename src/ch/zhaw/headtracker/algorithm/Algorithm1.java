package ch.zhaw.headtracker.algorithm;

import ch.zhaw.headtracker.Segmentation;
import ch.zhaw.headtracker.animation.PictureShop;
import ch.zhaw.headtracker.image.Image;
import ch.zhaw.headtracker.image.ImageUtil;
import ch.zhaw.headtracker.image.ImageView;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static ch.zhaw.headtracker.algorithm.ControlPanel.*;

public final class Algorithm1 implements AlgorithmRunner.Algorithm {
	private final PictureShop pictureShop = new PictureShop("res/3d/jack/out/jack%04d.png", 0, 40);
	private final DropdownMenuSetting showImage = new DropdownMenuSetting("Show image", new String[]{"Original", "Mask", "Masked image", "Black / White"}, 0);
	private final CheckBoxSetting showAnimation = new CheckBoxSetting("Show animation", true);
	private final SliderSetting filterThreshold = new SliderSetting("Background threshold", 0, 50, 10);
	private final SliderSetting opening = new SliderSetting("Opening", 0, 50, 10);
	//	private final SliderSetting maximum = controlPanel.sliderSetting("Maximum", 0, 50, 10);
	private final SliderSetting contrastThreshold = new SliderSetting("Contrast threshold", 0, 255, 42);
	//	private final CheckBoxSetting showPlummets = controlPanel.checkBoxSetting("Show plummets", true);
	private final CheckBoxSetting showSegmentation = new CheckBoxSetting("Show segmentation", true);
	private final ButtonSetting resetBackground = new ButtonSetting("Reset Background");
	private Image background = null;

	private final List<EyePoint> eyePoints = new ArrayList<EyePoint>();
	private EyePoint leftEye = null;
	private EyePoint rightEye = null;
	private float headAngle = 0f;

	private int frame = 0;

	@Override
	public ControlPanel.Setting[] getSettings() {
		return new ControlPanel.Setting[]{showImage, filterThreshold, opening, contrastThreshold, showSegmentation, resetBackground};
	}

	@Override
	public ImageView.Painter run(final Image image) {
		frame++;

		if (resetBackground.getSignal() || background == null)
			background = new Image(image);

		final Image mask = new Image(image.width, image.height);

		for (int y = 0; y < image.height; y += 1) {
			for (int x = 0; x < image.width; x += 1) {
				int bgPixel = background.getPixel(x, y);
				int imgPixel = image.getPixel(x, y);

				mask.setPixel(x, y, Math.abs(bgPixel - imgPixel) < filterThreshold.value);
			}
		}

		ImageUtil.minimum(mask, opening.value);
		ImageUtil.maximum(mask, opening.value * 2);
		ImageUtil.minimum(mask, opening.value);

		final Image maskedImage = new Image(image);
		ImageUtil.bitOr(maskedImage, mask);

		final Image bwImage = new Image(maskedImage);
		ImageUtil.threshold(bwImage, contrastThreshold.value);

		final Set<Segmentation.Group> groups = Segmentation.findGroups(bwImage);

		for (Segmentation.Group i : groups) {
			boolean match = true;

			if (i.sum < 30 || i.sum > 200)
				match = false;
			if ((float) (i.right - i.left) / (i.bottom - i.top) < 1)
				match = false;
			if ((float) (i.right - i.left) / (i.bottom - i.top) > 3)
				match = false;

			if (match) {
				EyePoint eyePoint = new EyePoint();
				eyePoint.setGroup(i);
				addEyePoint(eyePoint);
			}
		}
		cleanUpEyePoints();
		calculateEyePoints();


		return new ImageView.Painter() {
			@Override
			public void draw(Graphics2D g2) {
				if (showAnimation.value) {
					g2.drawImage(pictureShop.getImageForAngle(headAngle), 0, 480 / 8 * 3, 752 / 8, 480 / 8, null);
				}

				for (EyePoint eyePoint : eyePoints) {
					g2.setPaint(Color.blue);
					g2.setStroke(new BasicStroke(0.5f));
					eyePoint.drawBounds(g2);
				}

				if (leftEye != null && rightEye != null) {
					float centerX = (rightEye.x + leftEye.x) / 2;
					float centerY = (rightEye.y + leftEye.y) / 2;

					int left = distanceLeft(mask, (int) centerX, (int) centerY);
					int right = distanceRight(mask, (int) centerX, (int) centerY);

					headAngle = (float) left / (left + right) * 180 - 90;

					if (showSegmentation.value) {
						g2.setStroke(new BasicStroke(1f));
						g2.setPaint(Color.red);
						leftEye.drawBounds(g2);
						g2.setPaint(Color.green);
						rightEye.drawBounds(g2);

						g2.setPaint(Color.blue);
						g2.draw(new Line2D.Double((int) centerX, (int) centerY - 4, (int) centerX, (int) centerY + 4));
						g2.draw(new Line2D.Double((int) centerX, (int) centerY, (int) centerX - left, (int) centerY));
						g2.draw(new Line2D.Double((int) centerX - left, (int) centerY, (int) centerX - left + 4, (int) centerY - 4));
						g2.draw(new Line2D.Double((int) centerX - left, (int) centerY, (int) centerX - left + 4, (int) centerY + 4));
						g2.draw(new Line2D.Double((int) centerX, (int) centerY, (int) centerX + right, (int) centerY));
						g2.draw(new Line2D.Double((int) centerX + right, (int) centerY, (int) centerX + right - 4, (int) centerY - 4));
						g2.draw(new Line2D.Double((int) centerX + right, (int) centerY, (int) centerX + right - 4, (int) centerY + 4));
					}
				}
			}

			@Override
			protected Image getImage() {
				if (showImage.value == 0)
					return image;
				else if (showImage.value == 1)
					return mask;
				else if (showImage.value == 2)
					return maskedImage;
				else
					return bwImage;
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
		while (x >= 0) {
			if (mask.getPixel(x, y) > 0)
				return distance;
			distance++;
			x--;
		}
		return 0;
	}

	private static int distanceRight(Image mask, int x, int y) {
		int distance = 0;
		while (x < mask.width) {
			if (mask.getPixel(x, y) > 0)
				return distance;
			distance++;
			x++;
		}
		return 0;
	}

	private void addEyePoint(EyePoint eyePoint) {
		//System.out.println("add");
		boolean similar = false;
		for (EyePoint p : eyePoints) {
			if (eyePoint.getDistance(p) < 20) {
				p.setGroup(eyePoint.group);
				p.timestamp = frame;
				if (p.hitRatio < 1000)
					p.hitRatio++;
				similar = true;
				break;
			}
		}
		if (!similar)
			eyePoints.add(eyePoint);
	}

	private void cleanUpEyePoints() {
		final List<EyePoint> toBeRemoved = new ArrayList<EyePoint>();
		for (EyePoint eyePoint : eyePoints) {
			if (eyePoint.timestamp < frame - 2) {
				toBeRemoved.add(eyePoint);
			}
		}
		for (EyePoint eyePoint : toBeRemoved) {
			eyePoints.remove(eyePoint);
		}
	}

	private void calculateEyePoints() {
		final List<EyePoint> eyes = new ArrayList<EyePoint>();
		for (EyePoint eyePoint : eyePoints) {
			if (eyePoint.hitRatio > 5) {
				eyes.add(eyePoint);
			}
		}
		boolean isPossible = true;
		if (eyes.size() != 2)
			isPossible = false;
		else if (eyes.get(0).getDistance(eyes.get(1)) > 200)
			isPossible = false;
		else if (Math.abs(eyes.get(0).x - eyes.get(1).x) < 20)
			isPossible = false;
		else if (Math.abs((eyes.get(0).y - eyes.get(1).y) / (eyes.get(0).x - eyes.get(1).x)) > 0.5)
			isPossible = false;

		if (isPossible) {
			if (eyes.get(0).x < eyes.get(1).y) {
				leftEye = eyes.get(0);
				rightEye = eyes.get(1);
			} else {
				leftEye = eyes.get(1);
				rightEye = eyes.get(0);
			}
		} else {
			leftEye = null;
			rightEye = null;
		}
	}

	public final class EyePoint {
		public float x;
		public float y;
		public long timestamp;
		public int hitRatio = 0;
		public Segmentation.Group group;

		public EyePoint() {
			this.timestamp = frame;
			this.hitRatio = 0;
		}

		public void setGroup(Segmentation.Group group) {
			this.group = group;
			x = (float) group.posSumX / group.sum;
			y = (float) group.posSumY / group.sum;
		}

		public double getDistance(EyePoint other) {
			return Math.sqrt(Math.pow((double) other.x - x, 2) + Math.pow((double) other.y - y, 2));
		}

		public void drawBounds(Graphics2D g2) {
			g2.draw(new Rectangle2D.Double(group.left - .5, group.top - .5, group.right - group.left, group.bottom - group.top));
		}
	}
}
