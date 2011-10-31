package ch.zhaw.headtracker;

import java.io.IOException;

public interface ImageGrabber {
	Image getImage() throws IOException;
}
