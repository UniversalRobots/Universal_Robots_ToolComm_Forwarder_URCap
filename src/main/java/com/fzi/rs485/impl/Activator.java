// -- BEGIN LICENSE BLOCK ----------------------------------------------
// Copyright 2019 FZI Forschungszentrum Informatik
// Created on behalf of Universal Robots A/S
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

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import com.ur.urcap.api.contribution.InstallationNodeService;
import com.ur.urcap.api.contribution.DaemonService;

public class Activator implements BundleActivator {
  @Override
  public void start(final BundleContext context) throws Exception {
    RS485DaemonService daemonService = new RS485DaemonService();
    RS485InstallationNodeService installationNodeService =
        new RS485InstallationNodeService(daemonService);

    context.registerService(InstallationNodeService.class, installationNodeService, null);
    // context.registerService(ProgramNodeService.class, new RS485ProgramNodeService(), null);
    context.registerService(DaemonService.class, daemonService, null);
  }

  @Override
  public void stop(BundleContext context) throws Exception {}
}
