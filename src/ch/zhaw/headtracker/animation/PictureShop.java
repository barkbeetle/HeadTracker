package ch.zhaw.headtracker.animation;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class PictureShop
{
	protected final int numberOfImages;
	protected final BufferedImage[] images;

	public PictureShop(String formattedPath, int minIndex, int maxIndex)
	{
		this.numberOfImages = maxIndex - minIndex + 1;
		images = new BufferedImage[numberOfImages];

		try
		{
			for (int i = 0; i < numberOfImages; i++)
			{
				images[i] = ImageIO.read(new File(String.format(formattedPath, i)));
			}
		}
		catch (IOException e)
		{
			throw new RuntimeException(e);
		}
	}

	public BufferedImage getImageForAngle(float angle)
	{
		if (angle < -90f || angle > 90f)
			throw new RuntimeException("Angle must have a value between -90 and 90 degrees.");

		int imageIndex = (int) ((angle + 90) / (180f / (numberOfImages - 1)));
		return images[imageIndex];
	}
}
