package ch.zhaw.headtracker.grabber;

import ch.zhaw.headtracker.algorithm.ControlPanel;
import ch.zhaw.headtracker.image.Image;
import java.io.*;
import java.net.*;

public final class SocketImageGrabber implements ImageGrabber {
	private final InputStream input;
	private final int width;
	private final int height;
	private final ControlPanel.CheckBoxSetting stillFrame = new ControlPanel.CheckBoxSetting("Still frame", false);
	private Image lastImage = null;

	@SuppressWarnings({ "SocketOpenedButNotSafelyClosed" })
	public SocketImageGrabber(String address, short port, int exposureTimeMicroseconds, int width, int height) throws IOException {
		this.width = width;
		this.height = height;

		Socket socket = new Socket(InetAddress.getByName(address), port);

		PrintStream output = new PrintStream(socket.getOutputStream());

		// Sent to the leanXcam to set exposure time
		output.println(exposureTimeMicroseconds);
		output.flush();
		socket.getOutputStream().flush();

		input = socket.getInputStream();
	}
	
	@Override
	public Image getImage() throws IOException {
		if (lastImage == null || !stillFrame.value)
			lastImage = Image.readFromStream(input, width, height);
		
		return lastImage;
	}

	@Override
	public ControlPanel.Setting[] getSettings() {
		return new ControlPanel.Setting[] { stillFrame };
	}
}
