package com.aica.aicasystemclient.impl;

import com.ur.urcap.api.contribution.DaemonContribution;
import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.installation.CreationContext;
import com.ur.urcap.api.contribution.installation.InstallationAPIProvider;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.domain.userinteraction.inputvalidation.InputValidationFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputCallback;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardInputFactory;
import com.ur.urcap.api.domain.userinteraction.keyboard.KeyboardTextInput;

import java.awt.EventQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class AICASystemClientInstallationNodeContribution implements InstallationNodeContribution {
	private static final String IP_KEY = "aicacoreip";
	private static final String KEY_KEY = "aicaapikey";
	private static final String XMLRPC_VARIABLE = "aica";
	private static final String ENABLED_KEY = "enabled";
	private static final long DAEMON_TIME_OUT_NANO_SECONDS = TimeUnit.SECONDS.toNanos(20);
	private static final long RETRY_TIME_TO_WAIT_MILLI_SECONDS = TimeUnit.SECONDS.toMillis(1);

	private final AICASystemClientInstallationNodeView view;
	private final AICASystemClientDaemonService daemonService;
	private final InputValidationFactory inputValidationFactory;
	private final DataModel model;
	private final XmlRpcAICASystemClientInterface daemonStatusMonitor;
	private final KeyboardInputFactory keyboardInputFactory;
	private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
	private ScheduledFuture<?> scheduleAtFixedRate;

	public AICASystemClientInstallationNodeContribution(InstallationAPIProvider apiProvider,
												AICASystemClientInstallationNodeView view,
												DataModel model,
												AICASystemClientDaemonService daemonService,
												XmlRpcAICASystemClientInterface xmlRpcAICASystemClientInterface,
												CreationContext context) {
		keyboardInputFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getKeyboardInputFactory();
		inputValidationFactory = apiProvider.getUserInterfaceAPI().getUserInteraction().getInputValidationFactory();
		this.view = view;
		this.model = model;
		this.daemonService = daemonService;
		this.daemonStatusMonitor = xmlRpcAICASystemClientInterface;
		applyDesiredDaemonStatus();
	}

	@Override
	public void openView() {
		view.setIpText(getIp());
		view.setKeyText(getKey());
		daemonStatusMonitor.startMonitorThread();

		//UI updates from non-GUI threads must use EventQueue.invokeLater (or SwingUtilities.invokeLater)
		Runnable updateUIRunnable = new Runnable() {
			@Override
			public void run() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateUI();
					}
				});
			}
		};
		if (scheduleAtFixedRate != null) {
			scheduleAtFixedRate.cancel(true);
		}
		scheduleAtFixedRate = executorService.scheduleAtFixedRate(updateUIRunnable, 0, 1, TimeUnit.SECONDS);
	}

	@Override
	public void closeView() {
		if (scheduleAtFixedRate != null) {
			scheduleAtFixedRate.cancel(true);
		}
		daemonStatusMonitor.stopMonitorThread();
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.assign(XMLRPC_VARIABLE, "rpc_factory(\"xmlrpc\", \"" + XmlRpcAICASystemClientInterface.getDaemonUrl() + "\")");
		// Apply the settings to the daemon on program start in the Installation pre-amble
		// writer.appendLine(XMLRPC_VARIABLE + ".set_title(\"" + getPopupTitle() + "\")");
	}

	private void updateUI() {
		DaemonContribution.State state = getDaemonState();

		String text = "";
		switch (state) {
			case RUNNING:
				view.setStartButtonEnabled(false);
				view.setStopButtonEnabled(true);
				text = "XMLRPC server runs";
				break;
			case STOPPED:
				view.setStartButtonEnabled(true);
				view.setStopButtonEnabled(false);
				text = "XMLRPC server stopped";
				break;
			case ERROR:
			default:
				view.setStartButtonEnabled(true);
				view.setStopButtonEnabled(false);
				text = "XMLRPC server failed";
				break;
		}

		view.setStatusLabel(text);
	}

	public void onStartClick() {
		model.set(ENABLED_KEY, true);
		applyDesiredDaemonStatus();
	}

	public void onStopClick() {
		model.set(ENABLED_KEY, false);
		applyDesiredDaemonStatus();
	}

	private void applyDesiredDaemonStatus() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (AICASystemClientInstallationNodeContribution.this.isDaemonEnabled()) {
					try {
						AICASystemClientInstallationNodeContribution.this.awaitDaemonRunning();
					} catch (Exception e) {
						Thread.currentThread().interrupt();
					}
				} else {
					daemonService.getDaemon().stop();
				}
			}
		}).start();
	}

	private void awaitDaemonRunning() throws InterruptedException {
		daemonService.getDaemon().start();
		long endTime = System.nanoTime() + DAEMON_TIME_OUT_NANO_SECONDS;
		while (System.nanoTime() < endTime) {
			if (daemonStatusMonitor.isDaemonReachable()) {
				break;
			}
			Thread.sleep(RETRY_TIME_TO_WAIT_MILLI_SECONDS);
		}
	}

	public String getIp() {
		return model.get(IP_KEY, "");
	}

	public String getKey() {
		return model.get(KEY_KEY, "");
	}

	private void setIp(String title) {
		model.set(IP_KEY, title);
	}

	private void setKey(String title) {
		model.set(KEY_KEY, title);
	}

	public KeyboardTextInput getInputForIpTextField() {
		KeyboardTextInput keyboardInput = keyboardInputFactory.createStringKeyboardInput();
		keyboardInput.setErrorValidator(inputValidationFactory.createStringLengthValidator(1, 255));
		keyboardInput.setInitialValue(getIp());
		return keyboardInput;
	}

	public KeyboardInputCallback<String> getCallbackForIpTextField() {
		return new KeyboardInputCallback<String>() {
			@Override
			public void onOk(String value) {
				setIp(value);
				view.setIpText(value);
			}
		};
	}

	public KeyboardTextInput getInputForKeyTextField() {
		KeyboardTextInput keyboardInput = keyboardInputFactory.createStringKeyboardInput();
		keyboardInput.setErrorValidator(inputValidationFactory.createStringLengthValidator(1, 255));
		keyboardInput.setInitialValue(getKey());
		return keyboardInput;
	}

	public KeyboardInputCallback<String> getCallbackForKeyTextField() {
		return new KeyboardInputCallback<String>() {
			@Override
			public void onOk(String value) {
				setKey(value);
				view.setKeyText(value);
			}
		};
	}

	public boolean isIpSet() {
		return model.isSet(IP_KEY);
	}

	public boolean isKeySet() {
		return model.isSet(KEY_KEY);
	}

	private DaemonContribution.State getDaemonState() {
		return daemonStatusMonitor.isDaemonReachable() ? daemonService.getDaemon().getState() : DaemonContribution.State.STOPPED;
	}

	private Boolean isDaemonEnabled() {
		return model.get(ENABLED_KEY, true);
	}

	public String getXMLRPCVariable() {
		return XMLRPC_VARIABLE;
	}

	public XmlRpcAICASystemClientInterface getDaemonStatusMonitor() {
		return daemonStatusMonitor;
	}
}
