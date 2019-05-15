// -- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2019 FZI Forschungszentrum Informatik
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
// -- END LICENSE BLOCK ------------------------------------------------

//----------------------------------------------------------------------
/*!\file
 *
 * \author  Lea Steffen steffen@fzi.de
 * \date    2019-05-13
 *
 */
//----------------------------------------------------------------------
package com.fzi.rs485.impl;

import com.ur.urcap.api.contribution.DaemonContribution;
import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.domain.data.DataModel;
import com.ur.urcap.api.domain.script.ScriptWriter;
import com.ur.urcap.api.ui.annotation.Input;
import com.ur.urcap.api.ui.annotation.Label;
import com.ur.urcap.api.ui.component.InputButton;
import com.ur.urcap.api.ui.component.InputEvent;
import com.ur.urcap.api.ui.component.InputTextField;
import com.ur.urcap.api.ui.component.LabelComponent;

import java.awt.EventQueue;
import java.util.Timer;
import java.util.TimerTask;

public class RS485InstallationNodeContribution implements InstallationNodeContribution {
	private static final String POPUPTITLE_KEY = "popuptitle";

	private static final String XMLRPC_VARIABLE = "my_daemon";
	private static final String ENABLED_KEY = "enabled";
	private static final String DEFAULT_VALUE = "HelloWorld";

	private DataModel model;
	private final RS485DaemonService daemonService;
	private XmlRpcRS485Interface xmlRpcDaemonInterface;
	private Timer uiTimer;

	public RS485InstallationNodeContribution(RS485DaemonService daemonService, DataModel model) {
		this.daemonService = daemonService;
		this.model = model;
		xmlRpcDaemonInterface = new XmlRpcRS485Interface("127.0.0.1", 40404);
		applyDesiredDaemonStatus();
	}

	@Input(id = POPUPTITLE_KEY)
	private InputTextField popupTitleField;

	@Input(id = "btnEnableDaemon")
	private InputButton enableDaemonButton;

	@Input(id = "btnDisableDaemon")
	private InputButton disableDaemonButton;

	@Label(id = "lblDaemonStatus")
	private LabelComponent daemonStatusLabel;

	@Input(id = POPUPTITLE_KEY)
	public void onMessageChange(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setPopupTitle(popupTitleField.getText());
		}
	}

	@Input(id = "btnEnableDaemon")
	public void onStartClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setDaemonEnabled(true);
			applyDesiredDaemonStatus();
		}
	}

	@Input(id = "btnDisableDaemon")
	public void onStopClick(InputEvent event) {
		if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
			setDaemonEnabled(false);
			applyDesiredDaemonStatus();
		}
	}

	@Override
	public void openView() {
		enableDaemonButton.setText("Start Daemon");
		disableDaemonButton.setText("Stop daemon");
		popupTitleField.setText(getPopupTitle());

		//UI updates from non-GUI threads must use EventQueue.invokeLater (or SwingUtilities.invokeLater)
		uiTimer = new Timer(true);
		uiTimer.schedule(new TimerTask() {
			@Override
			public void run() {
				EventQueue.invokeLater(new Runnable() {
					@Override
					public void run() {
						updateUI();
					}
				});
			}
		}, 0, 1000);
	}

	private void updateUI() {
		DaemonContribution.State state = getDaemonState();

		if (state == DaemonContribution.State.RUNNING) {
			enableDaemonButton.setEnabled(false);
			disableDaemonButton.setEnabled(true);
		} else {
			enableDaemonButton.setEnabled(true);
			disableDaemonButton.setEnabled(false);
		}

		String text = "";
		switch (state) {
		case RUNNING:
			text = "My Daemon runs";
			break;
		case STOPPED:
			text = "My Daemon stopped";
			break;
		case ERROR:
			text = "My Daemon failed";
			break;
		}
		daemonStatusLabel.setText(text);
	}

	@Override
	public void closeView() {
		if (uiTimer != null) {
			uiTimer.cancel();
		}
	}

	public boolean isDefined() {
		return !getPopupTitle().isEmpty() && getDaemonState() == DaemonContribution.State.RUNNING;
	}

	@Override
	public void generateScript(ScriptWriter writer) {
		writer.globalVariable(XMLRPC_VARIABLE, "rpc_factory(\"xmlrpc\", \"http://127.0.0.1:40404/RPC2\")");
		// Apply the settings to the daemon on program start in the Installation pre-amble
		writer.appendLine(XMLRPC_VARIABLE + ".set_title(\"" + getPopupTitle() + "\")");
	}

	public String getPopupTitle() {
		if (!model.isSet(POPUPTITLE_KEY)) {
			resetToDefaultValue();
		}
		return model.get(POPUPTITLE_KEY, DEFAULT_VALUE);
	}

	private void setPopupTitle(String title) {
		if ("".equals(title)) {
			resetToDefaultValue();
		} else {
			model.set(POPUPTITLE_KEY, title);
			// Apply the new setting to the daemon for real-time preview purposes
			// Note this might influence a running program, since the actual state is stored in the daemon.
			setDaemonTitle(title);
		}
	}

	private void resetToDefaultValue() {
		popupTitleField.setText(DEFAULT_VALUE);
		model.set(POPUPTITLE_KEY, DEFAULT_VALUE);
		setDaemonTitle(DEFAULT_VALUE);
	}

	private void setDaemonTitle(String title) {
		try {
			xmlRpcDaemonInterface.setTitle(title);
		} catch(Exception e){
			System.err.println("Could not set the title in the daemon process.");
		}
	}

	private void applyDesiredDaemonStatus() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				if (isDaemonEnabled()) {
					// Download the daemon settings to the daemon process on initial start for real-time preview purposes
					try {
						awaitDaemonRunning(5000);
						xmlRpcDaemonInterface.setTitle(getPopupTitle());
					} catch (Exception e) {
						System.err.println("Could not set the title in the daemon process.");
					}
				} else {
					daemonService.getDaemon().stop();
				}
			}
		}).start();
	}

	private void awaitDaemonRunning(long timeOutMilliSeconds) throws InterruptedException {
		daemonService.getDaemon().start();
		long endTime = System.nanoTime() + timeOutMilliSeconds * 1000L * 1000L;
		while(System.nanoTime() < endTime && (daemonService.getDaemon().getState() != DaemonContribution.State.RUNNING || !xmlRpcDaemonInterface.isReachable())) {
			Thread.sleep(100);
		}
	}

	private DaemonContribution.State getDaemonState(){
		return daemonService.getDaemon().getState();
	}

	private Boolean isDaemonEnabled() {
		return model.get(ENABLED_KEY, true); //This daemon is enabled by default
	}

	private void setDaemonEnabled(Boolean enable) {
		model.set(ENABLED_KEY, enable);
	}

	public String getXMLRPCVariable(){
		return XMLRPC_VARIABLE;
	}

	public XmlRpcRS485Interface getXmlRpcDaemonInterface() {return xmlRpcDaemonInterface; }
}
