package com.aica.aicasystemclient.impl;

import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AICASystemClientInstallationNodeView implements SwingInstallationNodeView<AICASystemClientInstallationNodeContribution> {

	private final Style style;
	private JTextField ipInputField;
	private JTextField keyInputField;
	private JButton startButton;
	private JButton stopButton;
	private JLabel statusLabel;

	public AICASystemClientInstallationNodeView(Style style) {
		this.style = style;
	}

	@Override
	public void buildUI(JPanel panel, AICASystemClientInstallationNodeContribution contribution) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(createInfo());
		panel.add(createVerticalSpacing());

		panel.add(createIpInput(contribution));
		panel.add(createVerticalSpacing());
		panel.add(createKeyInput(contribution));
		panel.add(createVerticalSpacing(style.getLargeVerticalSpacing()));

		panel.add(createStartStopButtons(contribution));
		panel.add(createVerticalSpacing());

		panel.add(createStatusInfo());
	}

	private Box createInfo() {
		Box infoBox = Box.createVerticalBox();
		infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		JTextPane pane = new JTextPane();
		pane.setBorder(BorderFactory.createEmptyBorder());
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setLineSpacing(attributeSet, 0.5f);
		StyleConstants.setLeftIndent(attributeSet, 0f);
		pane.setParagraphAttributes(attributeSet, false);
		pane.setText("Configure the AICA System Client.");
		pane.setEditable(false);
		pane.setMaximumSize(pane.getPreferredSize());
		pane.setBackground(infoBox.getBackground());
		infoBox.add(pane);
		return infoBox;
	}

	private Box createIpInput(final AICASystemClientInstallationNodeContribution contribution) {
		Box inputBox = Box.createHorizontalBox();
		inputBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		inputBox.add(new JLabel("AICA Core IP:"));
		inputBox.add(createHorizontalSpacing());

		ipInputField = new JTextField();
		ipInputField.setFocusable(false);
		ipInputField.setPreferredSize(style.getInputfieldSize());
		ipInputField.setMaximumSize(ipInputField.getPreferredSize());
		ipInputField.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				KeyboardTextInput keyboardInput = contribution.getInputForIpTextField();
				keyboardInput.show(ipInputField, contribution.getCallbackForIpTextField());
			}
		});
		inputBox.add(ipInputField);

		return inputBox;
	}

	private Box createKeyInput(final AICASystemClientInstallationNodeContribution contribution) {
		Box inputBox = Box.createHorizontalBox();
		inputBox.setAlignmentX(Component.LEFT_ALIGNMENT);

		inputBox.add(new JLabel("AICA API Key:"));
		inputBox.add(createHorizontalSpacing());

		keyInputField = new JTextField();
		keyInputField.setFocusable(false);
		keyInputField.setPreferredSize(style.getInputfieldSize());
		keyInputField.setMaximumSize(ipInputField.getPreferredSize());
		keyInputField.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				KeyboardTextInput keyboardInput = contribution.getInputForKeyTextField();
				keyboardInput.show(keyInputField, contribution.getCallbackForKeyTextField());
			}
		});
		inputBox.add(keyInputField);

		return inputBox;
	}

	private Box createStartStopButtons(final AICASystemClientInstallationNodeContribution contribution) {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);

		startButton = new JButton("Start XMLRPC Server");
		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contribution.onStartClick();
			}
		});
		box.add(startButton);

		box.add(createHorizontalSpacing());

		stopButton = new JButton("Stop XMLRPC Server");
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				contribution.onStopClick();
			}
		});
		box.add(stopButton);

		return box;
	}

	private Box createStatusInfo() {
		Box box = Box.createHorizontalBox();
		box.setAlignmentX(Component.LEFT_ALIGNMENT);

		statusLabel = new JLabel("XMLRPC Server status");
		box.add(statusLabel);
		return box;
	}

	private Component createHorizontalSpacing() {
		return Box.createRigidArea(new Dimension(style.getHorizontalSpacing(), 0));
	}

	private Component createVerticalSpacing(int space) {
		return Box.createRigidArea(new Dimension(0, space));
	}

	private Component createVerticalSpacing() {
		return createVerticalSpacing(style.getVerticalSpacing());
	}

	public void setIpText(String t) {
		ipInputField.setText(t);
	}

	public void setKeyText(String t) {
		keyInputField.setText(t);
	}

	public void setStartButtonEnabled(boolean enabled) {
		startButton.setEnabled(enabled);
	}

	public void setStopButtonEnabled(boolean enabled) {
		stopButton.setEnabled(enabled);
	}

	public void setStatusLabel(String text) {
		statusLabel.setText(text);
	}
}
