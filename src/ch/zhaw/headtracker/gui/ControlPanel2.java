package ch.zhaw.headtracker.gui;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;
import javax.jnlp.BasicService;
import javax.swing.*;
import javax.swing.event.*;

public final class ControlPanel2 {
	private final JDialog dialog = new JDialog();
	private final List<Setting> settings = new ArrayList<Setting>();
	
	public ControlPanel2() {
		dialog.setAlwaysOnTop(true);
		dialog.setResizable(false);
		dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.PAGE_AXIS));
		dialog.getRootPane().putClientProperty("Window.style", "small");
	}

	public void show(Point location) {
		dialog.add(Box.createVerticalStrut(10));

		for (Setting i : settings)
			dialog.add(i.makePanel());

		dialog.add(Box.createVerticalStrut(16));
		
		dialog.setLocation(location);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	public SliderSetting sliderSetting(String label, int minValue, int maxValue, int value) {
		SliderSetting res = new SliderSetting(label, minValue, maxValue, value);
		
		settings.add(res);
		
		return res;
	}

	public CheckBoxSetting checkBoxSetting(String label, boolean value) {
		CheckBoxSetting res = new CheckBoxSetting(label, value);

		settings.add(res);

		return res;
	}

	public ButtonSetting buttonSetting(String label) {
		ButtonSetting res = new ButtonSetting(label);

		settings.add(res);

		return res;
	}

	public DropdownMenuSetting dropdownMenuSetting(String label, String[] options, int value) {
		DropdownMenuSetting res = new DropdownMenuSetting(label, options, value);

		settings.add(res);

		return res;
	}
	
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

	public static final class SliderSetting extends Setting {
		public int value;
		public final int minValue;
		public final int maxValue;

		private SliderSetting(String label, int minValue, int maxValue, int value) {
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
			panel.add(setWidth(slider, 150));
			panel.add(setWidth(valueLabel, 35));
			
			return panel;
		}

		private String valueString() {
			return String.format("%d", value);
		}
	}

	public static final class CheckBoxSetting extends Setting {
		public boolean value;

		private CheckBoxSetting(String label, boolean value) {
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
			panel.add(setWidth(checkBox, 178));

			return panel;
		}
	}

	public static final class ButtonSetting extends Setting {
		private boolean signalPending = false;

		private ButtonSetting(String label) {
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

		private DropdownMenuSetting(String label, String[] options, int value) {
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
			panel.add(Box.createHorizontalStrut(176 - comboBox.getPreferredSize().width));

			return panel;
		}
	}
}
