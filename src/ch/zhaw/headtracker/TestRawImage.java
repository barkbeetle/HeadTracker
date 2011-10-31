package ch.zhaw.headtracker;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public final class TestRawImage extends JPanel
{
	private BufferedImage img = null;

	public TestRawImage(int width, int height) {
		JFrame frame = new JFrame("TestRawImage");
		frame.setLocation(200, 200);
		frame.setSize(width, height);

		frame.add(this);
		frame.setVisible(true);
	}
	
	public void setImage(final BufferedImage image) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				img = image;
				repaint();
			}
		});
	}

	@Override
	public void paint(Graphics g) {
		setBackground(Color.WHITE);

		if (img != null)
		{
			g.drawImage(img, 0, 0, null);
		}
	}

	public static void main(String args[])
	{
		new TestRawImage(300, 200);
	}
}
