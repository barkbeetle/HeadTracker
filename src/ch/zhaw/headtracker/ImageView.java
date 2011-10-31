package ch.zhaw.headtracker;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import javax.swing.SwingUtilities;

public final class ImageView {
	private final Frame frame;
	private final BufferedImage bufferedImage;
	private final int width;
	private final int height;

	public ImageView(int width, int height) {
		this.width = width;
		this.height = height;
		bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

		frame = new Frame("") {
			@Override
			public void paint(Graphics g) {
				g.drawImage(bufferedImage, 0, 0, null);
			}
		};

		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.setVisible(false);
				frame.dispose();
				
				// Hack, application doesn't quit on last window close
				System.exit(0);
			}
		});

		frame.setSize(width, height);
		frame.setVisible(true);
	}

	public void updateImage(final Image image) {
		assert image.width == width && image.height == height;

		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				DataBufferByte buffer = new DataBufferByte(image.getData(), image.getData().length);
				WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, width, 1, new int[] { 0 }, new Point());
				
				bufferedImage.setData(raster);

				frame.repaint();
			}
		});
	}
}
