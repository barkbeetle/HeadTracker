package ch.zhaw.headtracker.image;

import ch.zhaw.headtracker.gui.ControlPanel;
import java.io.IOException;

public interface ImageGrabber {
	Image getImage() throws IOException;
	ControlPanel.Setting[] getSettings();
}
