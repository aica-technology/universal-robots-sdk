package com.aica.aicasystemclient.impl;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AICASystemClientProgramNodeView implements SwingProgramNodeView<AICASystemClientProgramNodeContribution> {

	private final Style style;
	private JLabel ipPreviewLabel;
	private JLabel varPreviewLabel;

	public AICASystemClientProgramNodeView(Style style) {
		this.style = style;
	}

	@Override
	public void buildUI(JPanel panel, ContributionProvider<AICASystemClientProgramNodeContribution> provider) {
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(createInfo());
		panel.add(createVerticalSpacing(style.getExtraLargeVerticalSpacing()));

		panel.add(createPreviewInfo());
	}

	private Box createInfo() {
		Box infoBox = Box.createVerticalBox();
		infoBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		infoBox.add(new JLabel("This program node will connect to the AICA API using the XMLRPC server.\n" +
							   "Use the variable below to make appropriate XMLRPC calls in program nodes."));
		return infoBox;
	}

	private Box createPreviewInfo() {
		Box box = Box.createVerticalBox();

		JLabel preview = new JLabel("Current settings:");
		preview.setFont(preview.getFont().deriveFont((float)style.getSmallHeaderFontSize()).deriveFont(Font.BOLD));
		box.add(preview);

		box.add(createVerticalSpacing(style.getLargeVerticalSpacing()));

		Box titleBox = Box.createHorizontalBox();
		titleBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		titleBox.add(new JLabel("AICA Core IP:"));
		titleBox.add(createHorizontalSpacing());
		ipPreviewLabel = new JLabel();
		titleBox.add(ipPreviewLabel);
		box.add(titleBox);

		box.add(createVerticalSpacing());

		Box messageBox = Box.createHorizontalBox();
		messageBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		messageBox.add(new JLabel("XMLRPC Variable:"));
		messageBox.add(createHorizontalSpacing());
		varPreviewLabel = new JLabel();
		messageBox.add(varPreviewLabel);
		box.add(messageBox);

		return box;
	}

	private Component createHorizontalSpacing() {
		return Box.createRigidArea(new Dimension(style.getHorizontalSpacing(), 0));
	}

	private Component createVerticalSpacing() {
		return createVerticalSpacing(style.getVerticalSpacing());
	}

	private Component createVerticalSpacing(int space) {
		return Box.createRigidArea(new Dimension(0, space));
	}

	public void setIpPreview(String title) {
		ipPreviewLabel.setText(title);
	}

	public void setVarPreview(String title) {
		varPreviewLabel.setText(title);
	}
}
