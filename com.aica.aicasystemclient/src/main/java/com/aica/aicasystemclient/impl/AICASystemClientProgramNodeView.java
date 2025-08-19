package com.aica.aicasystemclient.impl;

import com.ur.urcap.api.contribution.ContributionProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeView;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;

import javax.swing.*;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class AICASystemClientProgramNodeView implements SwingProgramNodeView<AICASystemClientProgramNodeContribution> {

	private final Style style;
	private JLabel ipPreviewLabel;
	private JLabel keyPreviewLabel;
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
		JTextPane pane = new JTextPane();
		pane.setBorder(BorderFactory.createEmptyBorder());
		SimpleAttributeSet attributeSet = new SimpleAttributeSet();
		StyleConstants.setLineSpacing(attributeSet, 0.5f);
		StyleConstants.setLeftIndent(attributeSet, 0f);
		pane.setParagraphAttributes(attributeSet, false);
		pane.setText("This program node will connect to the AICA API using the XMLRPC server.\n" +
					 "Use the variable below to make appropriate XMLRPC calls in program nodes.");
		pane.setEditable(false);
		pane.setMaximumSize(pane.getPreferredSize());
		pane.setBackground(infoBox.getBackground());
		infoBox.add(pane);
		return infoBox;
	}

	private Box createPreviewInfo() {
		Box box = Box.createVerticalBox();

		JLabel preview = new JLabel("Current settings:");
		preview.setFont(preview.getFont().deriveFont((float)style.getSmallHeaderFontSize()).deriveFont(Font.BOLD));
		box.add(preview);

		box.add(createVerticalSpacing(style.getLargeVerticalSpacing()));

		Box ipBox = Box.createHorizontalBox();
		ipBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		ipBox.add(new JLabel("AICA Core IP:"));
		ipBox.add(createHorizontalSpacing());
		ipPreviewLabel = new JLabel();
		ipBox.add(ipPreviewLabel);
		box.add(ipBox);

		box.add(createVerticalSpacing());

		Box keyBox = Box.createHorizontalBox();
		keyBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		keyBox.add(new JLabel("AICA API Key:"));
		keyBox.add(createHorizontalSpacing());
		keyPreviewLabel = new JLabel();
		keyBox.add(keyPreviewLabel);
		box.add(keyBox);

		box.add(createVerticalSpacing());

		Box varBox = Box.createHorizontalBox();
		varBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		varBox.add(new JLabel("XMLRPC Variable:"));
		varBox.add(createHorizontalSpacing());
		varPreviewLabel = new JLabel();
		varBox.add(varPreviewLabel);
		box.add(varBox);

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

	public void setKeyPreview(String title) {
		keyPreviewLabel.setText(title);
	}

	public void setVarPreview(String title) {
		varPreviewLabel.setText(title);
	}
}
