package ch.zhaw.headtracker.algorithm;

import ch.zhaw.headtracker.image.*;
import java.awt.Point;
import java.util.*;

public class AlgorithmRunner {
	private AlgorithmRunner() {
	}

	public static void runAlgorithm(final Algorithm algorithm, final ImageGrabber grabber) {
		List<ControlPanel.Setting> settings = new ArrayList<ControlPanel.Setting>();

		settings.addAll(Arrays.asList(algorithm.getSettings()));
		settings.add(new ControlPanel.Heading("Grabber settings"));
		settings.addAll(Arrays.asList(grabber.getSettings()));

		final ImageView view = new ImageView(752, 480);
		ControlPanel controlPanel = new ControlPanel(settings.toArray(new ControlPanel.Setting[settings.size()]));

		view.show(new Point(80, 100));
		controlPanel.show(new Point(752 + 10 + 80, 100));

		Thread thread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (true) {
						view.update(algorithm.run(ImageUtil.scaleDown(grabber.getImage(), 2)));

						Thread.sleep(100);
					}
				} catch (Throwable e) {
					e.printStackTrace();
				}
			}
		});
		
		thread.start();
	}
	
	public interface Algorithm {
		ControlPanel.Setting[] getSettings();
		ImageView.Painter run(Image image);
	}
}
