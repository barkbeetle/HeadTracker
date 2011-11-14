package ch.zhaw.headtracker.grabber;

import ch.zhaw.headtracker.algorithm.ControlPanel;
import ch.zhaw.headtracker.image.Image;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.Socket;

public final class SocketImageGrabber implements ImageGrabber {
	private final int width;
	private final int height;
	private final String address;
	private final short port;
	private final ControlPanel.CheckBoxSetting stillFrame = new ControlPanel.CheckBoxSetting("Still frame", false);
	private final ExposureTimeSetting exposureTime = new ExposureTimeSetting("Exposure time [μs]", 50000);
	private Socket socket = null;
	private Image lastImage = null;
	
	public SocketImageGrabber(String address, short port, int width, int height) {
		this.address = address;
		this.port = port;
		this.width = width;
		this.height = height;
	}
	
	@SuppressWarnings({"SocketOpenedButNotSafelyClosed"})
	@Override
	public Image getImage() throws IOException {
		if (exposureTime.getSignal()) {
			socket.close();
			socket = null;
		}
		
		if (socket == null) {
			socket = new Socket(InetAddress.getByName(address), port);
			PrintStream output = new PrintStream(socket.getOutputStream());

			// Sent to the leanXcam to set exposure time
			output.println(Long.valueOf(exposureTime.value));
			output.flush();
			socket.getOutputStream().flush();
		}
		
		if (lastImage == null || !stillFrame.value)
			lastImage = Image.readFromStream(socket.getInputStream(), width, height);
		
		return lastImage;
	}

	@Override
	public ControlPanel.Setting[] getSettings() {
		return new ControlPanel.Setting[] { stillFrame, exposureTime };
	}

	public static final class ExposureTimeSetting extends ControlPanel.Setting {
		public long value;
		private boolean signalPending = false;

		public ExposureTimeSetting(String label, int value) {
			super(label);
			this.value = value;
		}

		public boolean getSignal() {
			boolean res = signalPending;

			signalPending = false;

			return res;
		}

		@Override
		public JPanel makePanel() {
			final JTextField textField = new JTextField(String.format("%d", value));
			textField.setPreferredSize(new Dimension(100, textField.getPreferredSize().height));
			textField.setMaximumSize(new Dimension(100, Integer.MAX_VALUE));

			JButton button = new JButton("set");
			button.putClientProperty("JButton.buttonType", "gradient");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					value = Long.valueOf(textField.getText());
					signalPending = true;
				}
			});

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createHorizontalGlue());
			panel.add(Box.createHorizontalStrut(16));
			panel.add(new JLabel(label));
			panel.add(Box.createHorizontalStrut(11));
			panel.add(textField);
			panel.add(button);
			panel.add(Box.createHorizontalStrut(ControlPanel.controlColumnWidth - 152));

			return panel;
		}
	}
}
