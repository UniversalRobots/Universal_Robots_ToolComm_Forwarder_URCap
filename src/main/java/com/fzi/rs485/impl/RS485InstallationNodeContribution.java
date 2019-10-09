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

import java.awt.EventQueue;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.xmlrpc.XmlRpcException;

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

public class RS485InstallationNodeContribution implements InstallationNodeContribution {
  private static final String XMLRPC_VARIABLE = "rs485";
  private static final String ENABLED_KEY = "enabled";
  private static final String TCP_PORT_KEY = "rs485_tcp_port";
  private static final int DEFAULT_TCP_PORT = 54321;

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

  @Label(id = "lblDaemonStatus")
    private LabelComponent daemonStatusLabel;
  @Input (id = "inputTcpPort")
    private InputTextField tcpPortInput;
  @Input(id = "btnEnableDaemon")
	private InputButton enableDaemonButton;
  @Input(id = "btnDisableDaemon")
	private InputButton disableDaemonButton;
  
  @Input (id = "inputTcpPort")
  public void onTcpPortInput(InputEvent event) {
	if (event.getEventType() == InputEvent.EventType.ON_CHANGE) {
      setTcpPort(Integer.parseInt(tcpPortInput.getText()));
      changeTcpPort();
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
      System.err.println("Disabled daemon.");
    }
  }

  @Override
  public void openView() {
    // UI updates from non-GUI threads must use EventQueue.invokeLater (or
    // SwingUtilities.invokeLater)
	tcpPortInput.setText(Integer.toString(getTcpPort()));
    enableDaemonButton.setText("Start Daemon");
    disableDaemonButton.setText("Stop daemon");
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
    // DaemonContribution.State state = getDaemonState();
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
      text = "The RS-485 daemon is running.";
      break;
    case STOPPED:
      text = "The RS-485 daemon is not running.";
      break;
    //case ERROR:
    //    text = "The RS-485 stopped due to an error.";
    //    break;
    default:
      text = "The RS-485 daemon is ...";
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
    return getDaemonState() == DaemonContribution.State.RUNNING;
  }

  @Override
  public void generateScript(ScriptWriter writer) {
  }

  private void applyDesiredDaemonStatus() {
    new Thread(new Runnable() {
      @Override
      public void run() {
        if (isDaemonEnabled()) {
          // Download the daemon settings to the daemon process on initial start for real-time
          // preview purposes
          try {
            awaitDaemonRunning(5000);
          } catch (Exception e) {
            System.err.println("Exception in method 'applyDesiredDaemonStatus'.");
          }
        } else {
          daemonService.getDaemon().stop();
        }
      }
    }).start();
  }

  private void awaitDaemonRunning(long timeOutMilliSeconds) throws InterruptedException {
	  System.err.println("Firing up daemon.");
    daemonService.getDaemon().start();
    long endTime = System.nanoTime() + timeOutMilliSeconds * 1000L * 1000L;
    while (System.nanoTime() < endTime
        && (daemonService.getDaemon().getState() != DaemonContribution.State.RUNNING
               || !xmlRpcDaemonInterface.isReachable())) {
      Thread.sleep(100);
    }
    changeTcpPort();
  }

  private DaemonContribution.State getDaemonState() {
    return daemonService.getDaemon().getState();
  }

  
  private int getTcpPort() {
	return model.get(TCP_PORT_KEY, DEFAULT_TCP_PORT);
  }
  
  private void setTcpPort(int port) {
    model.set(TCP_PORT_KEY, port);
  }
  
  private Boolean isDaemonEnabled() {
    return model.get(ENABLED_KEY, true); //This daemon is enabled by default
  }

  private void setDaemonEnabled(Boolean enable) {
    model.set(ENABLED_KEY, enable);
  }

  public String getXMLRPCVariable() {
    return XMLRPC_VARIABLE;
  }

  public XmlRpcRS485Interface getXmlRpcDaemonInterface() {
    return xmlRpcDaemonInterface;
  }
  
  private void changeTcpPort() {
	String port = Integer.toString(getTcpPort()); 
	try {
	   xmlRpcDaemonInterface.setTcpPort(port);
	} catch (XmlRpcException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
    } catch (UnknownResponseException e) {
	  // TODO Auto-generated catch block
	  e.printStackTrace();
	}
  }
}
