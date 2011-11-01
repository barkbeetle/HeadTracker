package ch.zhaw.headtracker;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import javax.swing.SwingUtilities;

public final class ImageView {
	private final Frame frame;
	private Painter painter = null;

	public ImageView(final int width, final int height) {
		frame = new Frame("") {
			@Override
			public void paint(Graphics g) {
				if (painter == null)
					return;
				
				Graphics2D g2 = (Graphics2D) g;
				Painter currentPainter = painter;
				BufferedImage bufferedImage = currentPainter.bufferedImage;

				g.drawImage(bufferedImage, 0, 0, width, height, null);

				g2.setTransform(AffineTransform.getScaleInstance((double) width / bufferedImage.getWidth(), (double) height / bufferedImage.getHeight()));
				
				currentPainter.draw((Graphics2D) g);
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

	public void update(Painter painter) {
		this.painter = painter;
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.repaint();
			}
		});
	}
	
	public abstract static class Painter {
		public final BufferedImage bufferedImage;

		protected Painter(Image image) {
			byte[] data = image.getData();
			int width = image.width;
			int height = image.height;
			
			DataBufferByte buffer = new DataBufferByte(data, data.length);
			WritableRaster raster = Raster.createInterleavedRaster(buffer, width, height, width, 1, new int[] { 0 }, new Point());
			bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);

			bufferedImage.setData(raster);
		}
		
		public abstract void draw(Graphics2D graphics);
	}
}
