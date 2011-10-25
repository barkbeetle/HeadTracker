package ch.zhaw.headtracker;

import com.sun.image.codec.jpeg.JPEGCodec;
import com.sun.image.codec.jpeg.JPEGImageDecoder;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;

public class TestRawImage extends JPanel
{
	BufferedImage img = null;

	public TestRawImage()
	{
		JFrame frame = new JFrame("TestRawImage");
		frame.setLocation(200, 200);
		frame.setSize(500, 300);


		try
		{
			FileInputStream fis = new FileInputStream("simons-cat.jpg");
			BufferedInputStream bis = new BufferedInputStream(fis);
			JPEGImageDecoder jpegImageDecoder = JPEGCodec.createJPEGDecoder(bis);
			img = jpegImageDecoder.decodeAsBufferedImage();
			fis.close();
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}

		frame.add(this);
		frame.setVisible(true);


	}

	public synchronized void setImage(BufferedImage img)
	{
		this.img = img;
	}

	public synchronized void paintComponent(Graphics g)
	{
		super.paintComponent(g);
		this.setBackground(Color.WHITE);

		if (img != null)
		{
			g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), Color.BLACK, null);
		}
	}

	public static void main(String args[])
	{
		new TestRawImage();
	}
}
