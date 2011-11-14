package ch.zhaw.headtracker.algorithm;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class ControlPanel {
	private final JDialog dialog = new JDialog();
	
	public ControlPanel(Setting ... settings) {
		dialog.add(Box.createVerticalStrut(10));

		for (Setting i : settings)
			dialog.add(i.makePanel());

		dialog.add(Box.createVerticalStrut(16));
		
		dialog.setAlwaysOnTop(true);
		dialog.setResizable(false);
		dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.PAGE_AXIS));
		dialog.getRootPane().putClientProperty("Window.style", "small");
	}

	public void show(Point location) {
		dialog.setLocation(location);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	public static final int controlColumnWidth = 190;
	
	private static JComponent setWidth(JComponent component, int width) {
		component.setPreferredSize(new Dimension(width, component.getPreferredSize().height));
		component.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		
		return component;
	}

	public abstract static class Setting {
		public final String label;

		protected Setting(String label) {
			this.label = label;
		}

		public abstract JPanel makePanel();
	}
	
	public static final class Heading extends Setting {
		public Heading(String label) {
			super(label);
		}

		@Override
		public JPanel makePanel() {
			JLabel jlabel = new JLabel(label);
			jlabel.setFont(new Font("Lucida Grande", Font.BOLD, 12));
			
			JPanel innerPanel = new JPanel();
			innerPanel.setLayout(new BoxLayout(innerPanel, BoxLayout.LINE_AXIS));
			innerPanel.add(Box.createHorizontalStrut(10));
			innerPanel.add(jlabel);
			innerPanel.add(Box.createHorizontalGlue());
			
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
			panel.add(Box.createVerticalStrut(10));
			panel.add(innerPanel);
			panel.add(Box.createVerticalStrut(5));

			return panel;
		}
	}

	public static final class SliderSetting extends Setting {
		public int value;
		public final int minValue;
		public final int maxValue;

		public SliderSetting(String label, int minValue, int maxValue, int value) {
			super(label);
			this.value = value;
			this.minValue = minValue;
			this.maxValue = maxValue;
		}
		
		@Override
		public JPanel makePanel() {
			final JSlider slider = new JSlider(minValue, maxValue, value);
			final JLabel valueLabel = new JLabel(valueString());
			
			slider.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					value = slider.getValue();
					valueLabel.setText(valueString());
				}
			});

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createHorizontalGlue());
			panel.add(Box.createHorizontalStrut(16));
			panel.add(new JLabel(label));
			panel.add(setWidth(slider, controlColumnWidth - 40));
			panel.add(setWidth(valueLabel, 40));
			
			return panel;
		}

		private String valueString() {
			return String.format("%d", value);
		}
	}

	public static final class CheckBoxSetting extends Setting {
		public boolean value;

		public CheckBoxSetting(String label, boolean value) {
			super(label);
			this.value = value;
		}

		@Override
		public JPanel makePanel() {
			final JCheckBox checkBox = new JCheckBox(label, value);

			checkBox.addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent e) {
					value = checkBox.isSelected();
				}
			});

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createHorizontalGlue());
			panel.add(setWidth(checkBox, controlColumnWidth - 7));

			return panel;
		}
	}

	public static final class ButtonSetting extends Setting {
		private boolean signalPending = false;

		public ButtonSetting(String label) {
			super(label);
		}

		@Override
		public JPanel makePanel() {
			JButton button = new JButton(label);
			button.putClientProperty("JButton.buttonType", "gradient");
			button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					signalPending = true;
				}
			});

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createHorizontalGlue());
			panel.add(button);
			panel.add(Box.createHorizontalGlue());

			return panel;
		}

		public boolean getSignal() {
			boolean res = signalPending;

			signalPending = false;

			return res;
		}
	}

	public static final class DropdownMenuSetting extends Setting {
		public int value;
		private final String[] options;

		public DropdownMenuSetting(String label, String[] options, int value) {
			super(label);
			this.value = value;
			this.options = options;
		}

		@Override
		public JPanel makePanel() {
			final JComboBox comboBox = new JComboBox(options);

			comboBox.setSelectedIndex(value);
			comboBox.setMaximumSize(comboBox.getPreferredSize());
			comboBox.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					value = comboBox.getSelectedIndex();
				}
			});

			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createHorizontalGlue());
			panel.add(Box.createHorizontalStrut(16));
			panel.add(new JLabel(label));
			panel.add(Box.createHorizontalStrut(8));
			panel.add(comboBox);
			panel.add(Box.createHorizontalStrut(controlColumnWidth - comboBox.getPreferredSize().width - 8));

			return panel;
		}
	}

	public static final class TextFieldSetting extends Setting {
		public String value;

		public TextFieldSetting(String label, String value) {
			super(label);
			this.value = value;
		}

		@Override
		public JPanel makePanel() {
			final JTextField textField = new JTextField(value);
			textField.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					value = textField.getText();
				}
			});
			
			JPanel panel = new JPanel();
			panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
			panel.add(Box.createHorizontalGlue());
			panel.add(Box.createHorizontalStrut(16));
			panel.add(new JLabel(label));
			panel.add(Box.createHorizontalStrut(11));
			panel.add(setWidth(textField, 100));
			panel.add(Box.createHorizontalStrut(controlColumnWidth - 111));

			return panel;
		}
	}
}
