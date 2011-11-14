package ch.zhaw.headtracker.image;

import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.*;
import javax.swing.*;

public final class ImageView {
	private final JFrame frame;
	private Painter painter = null;

	public ImageView(int width, int height) {
		JPanel panel = new JPanel() {
			@Override
			public void paint(Graphics g) {
				if (painter != null)
					painter.drawPainter((Graphics2D) g, getWidth(), getHeight());
			}
		};
		
		panel.setPreferredSize(new Dimension(width, height));
		
		frame = new JFrame("Head Tracker");
		frame.setContentPane(panel);
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.setVisible(false);
				frame.dispose();
				
				// Hack, application doesn't quit on last window close
				System.exit(0);
			}
		});
	}
	
	public void show(Point location) {
		frame.setLocation(location);
		frame.pack();
		frame.setVisible(true);
	}

	public void update(Painter painter) {
		this.painter = painter;
		
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				frame.getContentPane().repaint();
			}
		});
	}
	
	public abstract static class Painter {
		public final void drawPainter(Graphics2D g2, int width, int height) {
			Image image = getImage();
			byte[] data = image.getData();
			
			DataBufferByte buffer = new DataBufferByte(data, data.length);
			WritableRaster raster = Raster.createInterleavedRaster(buffer, image.width, image.height, image.width, 1, new int[] { 0 }, new Point());
			BufferedImage bufferedImage = new BufferedImage(image.width, image.height, BufferedImage.TYPE_BYTE_GRAY);

			bufferedImage.setData(raster);
			
			double scale = Math.min((double) width / bufferedImage.getWidth(), (double) height / bufferedImage.getHeight());

			g2.setTransform(AffineTransform.getScaleInstance(scale, scale));
			g2.drawImage(bufferedImage, 0, 0, null);
			
			draw(g2);
		}
		
		protected abstract void draw(Graphics2D g2);
		protected abstract Image getImage();
	}
}
