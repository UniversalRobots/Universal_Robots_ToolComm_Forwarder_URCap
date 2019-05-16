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

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

public class XmlRpcRS485Interface {
  private final XmlRpcClient client;

  public XmlRpcRS485Interface(String host, int port) {
    XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
    config.setEnabledForExtensions(true);
    try {
      config.setServerURL(new URL("http://" + host + ":" + port + "/RPC2"));
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    config.setConnectionTimeout(1000); // 1s
    client = new XmlRpcClient();
    client.setConfig(config);
  }

  public boolean isReachable() {
    try {
      client.execute("get_title", new ArrayList<String>());
      return true;
    } catch (XmlRpcException e) {
      return false;
    }
  }

  public String getTitle() throws XmlRpcException, UnknownResponseException {
    Object result = client.execute("get_title", new ArrayList<String>());
    return processString(result);
  }

  public String setTitle(String title) throws XmlRpcException, UnknownResponseException {
    ArrayList<String> args = new ArrayList<String>();
    args.add(title);
    Object result = client.execute("set_title", args);
    return processString(result);
  }

  public String getMessage(String name) throws XmlRpcException, UnknownResponseException {
    ArrayList<String> args = new ArrayList<String>();
    args.add(name);
    Object result = client.execute("get_message", args);
    return processString(result);
  }

  private String processString(Object response) throws UnknownResponseException {
    if (response instanceof String) {
      return (String) response;
    } else {
      throw new UnknownResponseException();
    }
  }
}
