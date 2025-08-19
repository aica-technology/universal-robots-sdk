package com.aica.aicasystemclient.impl;

import com.ur.urcap.api.contribution.ViewAPIProvider;
import com.ur.urcap.api.contribution.installation.ContributionConfiguration;
import com.ur.urcap.api.contribution.installation.CreationContext;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.contribution.installation.swing.SwingInstallationNodeService;
import com.ur.urcap.api.domain.SystemAPI;
import com.ur.urcap.api.domain.data.DataModel;

import java.util.Locale;

public class AICASystemClientInstallationNodeService implements SwingInstallationNodeService<AICASystemClientInstallationNodeContribution, AICASystemClientInstallationNodeView> {

	private final AICASystemClientDaemonService daemonService;

	public AICASystemClientInstallationNodeService(AICASystemClientDaemonService daemonService) {
		this.daemonService = daemonService;
	}

	@Override
	public String getTitle(Locale locale) {
		return "AICA System Client";
	}

	@Override
	public void configureContribution(ContributionConfiguration configuration) {
	}

	@Override
	public AICASystemClientInstallationNodeView createView(ViewAPIProvider apiProvider) {
		SystemAPI systemAPI = apiProvider.getSystemAPI();
		Style style = systemAPI.getSoftwareVersion().getMajorVersion() >= 5 ? new V5Style() : new V3Style();
		return new AICASystemClientInstallationNodeView(style);
	}

	@Override
	public AICASystemClientInstallationNodeContribution createInstallationNode(InstallationAPIProvider apiProvider, AICASystemClientInstallationNodeView view, DataModel model, CreationContext context) {
		return new AICASystemClientInstallationNodeContribution(apiProvider, view, model, daemonService, new XmlRpcAICASystemClientInterface(), context);
	}

}
