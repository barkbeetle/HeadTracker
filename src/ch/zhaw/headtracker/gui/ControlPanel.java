package ch.zhaw.headtracker.gui;

import ch.zhaw.headtracker.Algorithm;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class ControlPanel extends JFrame implements ChangeListener
{
	private final JSlider filterThresholdSlider;
	private final JTextField filterThresholdTextField;

	private final JSlider minimumSlider;
	private final JTextField minimumTextField;

	private final JSlider maximumSlider;
	private final JTextField maximumTextField;

	public ControlPanel()
	{
		super("Control Panel");

		setSize(400, 300);
		setLayout(null);

		JLabel filterThresholdLabel = new JLabel("Filter Threshold:");
		filterThresholdLabel.setLocation(20, 20);
		filterThresholdLabel.setSize(200, 20);
		add(filterThresholdLabel);

		filterThresholdSlider = new JSlider(0, 50);
		filterThresholdSlider.setValue(Algorithm.filterThreshold);
		filterThresholdSlider.setLocation(150, 20);
		filterThresholdSlider.setSize(150, 20);
		filterThresholdSlider.addChangeListener(this);
		add(filterThresholdSlider);

		filterThresholdTextField = new JTextField(Algorithm.filterThreshold + "");
		filterThresholdTextField.setLocation(300, 20);
		filterThresholdTextField.setSize(80, 20);
		add(filterThresholdTextField);

		JLabel minimumLabel = new JLabel("Minimum:");
		minimumLabel.setLocation(20, 80);
		minimumLabel.setSize(200, 20);
		add(minimumLabel);

		minimumSlider = new JSlider(0, 20);
		minimumSlider.setValue(Algorithm.minimum);
		minimumSlider.setLocation(150, 80);
		minimumSlider.setSize(150, 20);
		minimumSlider.addChangeListener(this);
		add(minimumSlider);

		minimumTextField = new JTextField(Algorithm.minimum + "");
		minimumTextField.setLocation(300, 80);
		minimumTextField.setSize(80, 20);
		add(minimumTextField);

		JLabel maximumLabel = new JLabel("Maximum:");
		maximumLabel.setLocation(20, 140);
		maximumLabel.setSize(200, 20);
		add(maximumLabel);

		maximumSlider = new JSlider(0, 20);
		maximumSlider.setValue(Algorithm.maximum);
		maximumSlider.setLocation(150, 140);
		maximumSlider.setSize(150, 20);
		maximumSlider.addChangeListener(this);
		add(maximumSlider);

		maximumTextField = new JTextField(Algorithm.maximum + "");
		maximumTextField.setLocation(300, 140);
		maximumTextField.setSize(80, 20);
		add(maximumTextField);

		setVisible(true);
	}

	@Override
	public void stateChanged(ChangeEvent changeEvent)
	{
		if (changeEvent.getSource().equals(filterThresholdSlider))
		{
			Algorithm.filterThreshold = filterThresholdSlider.getValue();
			filterThresholdTextField.setText(Algorithm.filterThreshold + "");
		}

		if (changeEvent.getSource().equals(minimumSlider))
		{
			Algorithm.minimum = minimumSlider.getValue();
			minimumTextField.setText(Algorithm.minimum + "");
		}

		if (changeEvent.getSource().equals(maximumSlider))
		{
			Algorithm.maximum = maximumSlider.getValue();
			maximumTextField.setText(Algorithm.maximum + "");
		}
	}
}
