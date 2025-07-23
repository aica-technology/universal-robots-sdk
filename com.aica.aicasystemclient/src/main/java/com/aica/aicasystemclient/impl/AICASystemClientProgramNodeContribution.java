package com.aica.aicasystemclient.impl;

import com.ur.urcap.api.contribution.ProgramNodeContribution;
import com.ur.urcap.api.contribution.program.ProgramAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;

import java.awt.EventQueue;

public class AICASystemClientProgramNodeContribution implements ProgramNodeContribution {
	private final ProgramAPIProvider apiProvider;
	private final AICASystemClientProgramNodeView view;
	private final DataModel model;
	private final XmlRpcAICASystemClientInterface daemonStatusMonitor;
	private final KeyboardInputFactory keyboardInputFactory;

	public AICASystemClientProgramNodeContribution(ProgramAPIProvider apiProvider,
										   AICASystemClientProgramNodeView view,
										   DataModel model) {
		keyboardInputFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
		this.apiProvider = apiProvider;
		this.view = view;
		this.model = model;
		this.daemonStatusMonitor = getInstallation().getDaemonStatusMonitor();

	}

	@Override
	public void openView() {
		daemonStatusMonitor.startMonitorThread();

		//UI updates from non-GUI threads must use EventQueue.invokeLater (or SwingUtilities.invokeLater)
		updateUI();
	}

	private void updateUI() {
		if (getInstallation().isIpSet()) {
			setIpAndVar(getInstallation().getIp(), getInstallation().getXMLRPCVariable());
		} else {
			setIpAndVar("Set the AICA Core IP in the installation node", getInstallation().getXMLRPCVariable());
		}
		
	}

	private void setIpAndVar(final String ip, final String var) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				AICASystemClientProgramNodeContribution.this.updatePreview(ip, var);
			}
		});
	}

	private void updatePreview(String ip, String var) {
		view.setIpPreview(ip);
		view.setVarPreview(var);
		updateUI();
	}

	@Override
	public void closeView() {
		daemonStatusMonitor.stopMonitorThread();
	}

	@Override
	public String getTitle() {
		return "Connect to AICA API";
	}

	@Override
	public boolean isDefined() {
		return daemonStatusMonitor.isDaemonReachable() && getInstallation().isIpSet();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		// Interact with the daemon process through XML-RPC calls
		writer.assign("aica_success", getInstallation().getXMLRPCVariable() + ".initialize(\"" + getInstallation().getIp() + "\", \"" + getInstallation().getKey() + "\")");
		writer.appendLine("if not aica_success:");
		writer.appendLine("  message = \"Failed to connect to AICA API: " + getInstallation().getIp() + "\"");
		writer.appendLine("  title = \"AICA API Connection Error\"");
		writer.appendLine("  popup(message, title=title, warning=False, error=True, blocking=True)");
		writer.appendLine("end");
	}

	private AICASystemClientInstallationNodeContribution getInstallation() {
		return apiProvider.getProgramAPI().getInstallationNode(AICASystemClientInstallationNodeContribution.class);
	}
}
