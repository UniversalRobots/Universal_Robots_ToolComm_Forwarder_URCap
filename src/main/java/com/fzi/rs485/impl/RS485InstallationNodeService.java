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

import com.ur.urcap.api.contribution.InstallationNodeContribution;
import com.ur.urcap.api.contribution.InstallationNodeService;
import com.ur.urcap.api.domain.URCapAPI;

import java.io.InputStream;

import com.ur.urcap.api.domain.data.DataModel;

public class RS485InstallationNodeService implements InstallationNodeService {
  private final RS485DaemonService daemonService;

  public RS485InstallationNodeService(RS485DaemonService daemonService) {
    this.daemonService = daemonService;
  }

  @Override
  public InstallationNodeContribution createInstallationNode(URCapAPI api, DataModel model) {
    return new RS485InstallationNodeContribution(daemonService, model);
  }

  @Override
  public String getTitle() {
    return "RS485";
  }

  @Override
  public InputStream getHTML() {
    InputStream is = this.getClass().getResourceAsStream("/com/fzi/rs485/impl/installation.html");
    return is;
  }
}
