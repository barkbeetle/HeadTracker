package ch.zhaw.headtracker;

import java.awt.Frame;
import java.awt.Graphics;
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
			}
		});

		frame.setSize(width, height);
		frame.setVisible(true);
	}
	
	public void updateImage(final Image image) {
	//	byte[] data = image.getData();
	//	DataBufferByte buffer = new DataBufferByte(data, data.length);
	//	WritableRaster raster = Raster.createWritableRaster(new SinglePixelPackedSampleModel(DataBuffer.TYPE_BYTE, ), );

		assert image.width == width && image.height == height;
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				for (int iy = 0; iy < height; iy += 1) {
					for (int ix = 0; ix < width; ix += 1) {
						int pixel = image.getPixel(ix, iy);

						bufferedImage.setRGB(ix, iy, (pixel & 0xff) << 16 | (pixel & 0xff) << 8 | (pixel & 0xff));
					}
				}

				frame.repaint();
			}
		});
	}
}
