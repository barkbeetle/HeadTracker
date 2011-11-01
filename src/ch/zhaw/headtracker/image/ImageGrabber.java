package ch.zhaw.headtracker.image;

import java.io.IOException;

public interface ImageGrabber {
	Image getImage() throws IOException;
}
