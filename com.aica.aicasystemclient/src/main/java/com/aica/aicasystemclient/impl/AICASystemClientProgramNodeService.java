package com.aica.aicasystemclient.impl;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.program.ContributionConfiguration;
import com.ur.urcap.api.contribution.program.CreationContext;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.contribution.program.swing.SwingProgramNodeService;
import com.ur.urcap.api.domain.SystemAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.util.Locale;

public class AICASystemClientProgramNodeService implements SwingProgramNodeService<AICASystemClientProgramNodeContribution, AICASystemClientProgramNodeView> {

	public AICASystemClientProgramNodeService() {
	}

	@Override
	public String getId() {
		return "ConnectAICAAPINode";
	}

	@Override
	public String getTitle(Locale locale) {
		return "Connect to AICA API";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
		configuration.setChildrenAllowed(false);
	}

	@Override
	public AICASystemClientProgramNodeView createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new AICASystemClientProgramNodeView(style);
	}

	@Override
	public AICASystemClientProgramNodeContribution createNode(ProgramAPIProvider apiProvider, AICASystemClientProgramNodeView view, DataModel model, CreationContext context) {
		return new AICASystemClientProgramNodeContribution(apiProvider, view, model);
	}

}
