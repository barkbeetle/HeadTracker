package ch.zhaw.headtracker.grabber;

import ch.zhaw.headtracker.algorithm.ControlPanel;
import ch.zhaw.headtracker.image.Image;
import java.io.IOException;

public interface ImageGrabber {
	Image getImage() throws IOException;
	ControlPanel.Setting[] getSettings();
}
