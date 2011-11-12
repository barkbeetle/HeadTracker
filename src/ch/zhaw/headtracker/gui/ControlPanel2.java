package ch.zhaw.headtracker.gui;

import java.awt.Dimension;
import java.awt.Point;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public final class ControlPanel2 {
	private final JDialog dialog = new JDialog();
	
	public ControlPanel2(Setting ... settings) {
	//	frame.setUndecorated(true);
		dialog.setAlwaysOnTop(true);
		dialog.setResizable(false);
		dialog.setLayout(new BoxLayout(dialog.getContentPane(), BoxLayout.PAGE_AXIS));
		dialog.getRootPane().putClientProperty("Window.style", "small");
		
		dialog.add(Box.createVerticalStrut(10));
		
		for (Setting i : settings)
			dialog.add(i.makePanel());
		
		dialog.add(Box.createVerticalStrut(16));
	}

	public void show(Point location) {
		dialog.setLocation(location);
		dialog.pack();
		dialog.setVisible(true);
	}
	
	public abstract static class Setting {
		public final String label;

		protected Setting(String label) {
			this.label = label;
		}
		
		public abstract JPanel makePanel();
	}
	
	private static JComponent setWidth(JComponent component, int width) {
		component.setPreferredSize(new Dimension(width, component.getPreferredSize().height));
		component.setMaximumSize(new Dimension(width, Integer.MAX_VALUE));
		
		return component;
	}

	public static final class SliderSetting extends Setting {
		public int value;
		public final int minValue;
		public final int maxValue;

		public SliderSetting(String label, int minValue, int maxValue) {
			this(label, maxValue, minValue, minValue);
		}

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
			panel.add(new JLabel(label + ":"));
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

		public CheckBoxSetting(String label) {
			this(label, false);
		}

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
			panel.add(setWidth(checkBox, 180));
			
			return panel;
		}
	}
}
