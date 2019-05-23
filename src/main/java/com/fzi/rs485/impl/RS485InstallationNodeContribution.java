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

public class RS485InstallationNodeContribution implements InstallationNodeContribution {
  private static final String XMLRPC_VARIABLE = "rs485";
  private static final String ENABLED_KEY = "enabled";

  private DataModel model;
  private final RS485DaemonService daemonService;
  private XmlRpcRS485Interface xmlRpcDaemonInterface;

  public RS485InstallationNodeContribution(RS485DaemonService daemonService, DataModel model) {
    this.daemonService = daemonService;
    this.model = model;
    xmlRpcDaemonInterface = new XmlRpcRS485Interface("127.0.0.1", 40404);
    applyDesiredDaemonStatus();
  }

  @Override
  public void openView() {
  }

  @Override
  public void closeView() {
  }

  public boolean isDefined() {
    return getDaemonState() == DaemonContribution.State.RUNNING;
  }

  @Override
  public void generateScript(ScriptWriter writer) {
    writer.globalVariable(
        XMLRPC_VARIABLE, "rpc_factory(\"xmlrpc\", \"http://127.0.0.1:40404/RPC2\")");
    
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
    daemonService.getDaemon().start();
    long endTime = System.nanoTime() + timeOutMilliSeconds * 1000L * 1000L;
    while (System.nanoTime() < endTime
        && (daemonService.getDaemon().getState() != DaemonContribution.State.RUNNING
               || !xmlRpcDaemonInterface.isReachable())) {
      Thread.sleep(100);
    }
  }

  private DaemonContribution.State getDaemonState() {
    return daemonService.getDaemon().getState();
  }

  private Boolean isDaemonEnabled() {
    return model.get(ENABLED_KEY, true); // This daemon is enabled by default
  }

  public String getXMLRPCVariable() {
    return XMLRPC_VARIABLE;
  }

  public XmlRpcRS485Interface getXmlRpcDaemonInterface() {
    return xmlRpcDaemonInterface;
  }
}
