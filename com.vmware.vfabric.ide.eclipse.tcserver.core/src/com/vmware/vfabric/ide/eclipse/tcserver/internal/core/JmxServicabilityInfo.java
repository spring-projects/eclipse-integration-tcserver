/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.tomcat.core.internal.TomcatPlugin;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.Listener;
import org.eclipse.osgi.util.NLS;

/**
 * Provides access to the configuration entry for the tc Server JMX service.
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class JmxServicabilityInfo implements IServicabilityInfo {

	private final Listener listener;

	private final Properties properties;

	public JmxServicabilityInfo(Listener listener, Properties properties) {
		this.listener = listener;
		this.properties = properties;
	}

	public String getAccessFile() {
		return listener.getAttributeValue("accessFile");
	}

	public List<JmxCredentials> getAllCredentials(TcServer server) {
		File accessFile = getFile(server, getAccessFile());
		File passwordFile = getFile(server, getPasswordFile());
		if (accessFile != null && passwordFile != null) {
			Map<String, String> userToPassword = read(passwordFile);
			Map<String, String> userToPermission = read(accessFile);
			List<JmxCredentials> credentialList = new ArrayList<JmxCredentials>(userToPassword.size());
			for (String user : userToPassword.keySet()) {
				JmxCredentials credentials = new JmxCredentials(user, userToPassword.get(user));
				credentials.setWriteAccess(JmxCredentials.WRITE_FLAG.equals(userToPermission.get(user)));
				credentialList.add(credentials);
			}
			return credentialList;
		}
		return null;
	}

	public String getConnectionLabel() {
		StringBuffer sb = new StringBuffer();
		String host = getHost();
		if (host != null) {
			sb.append(host);
		}
		String port = getPort();
		if (port != null) {
			sb.append(":");
			sb.append(port);
		}
		return sb.toString();
	}

	public JmxCredentials getCredentials(TcServer server) {
		List<JmxCredentials> credentialList = getAllCredentials(server);
		if (credentialList != null) {
			for (JmxCredentials credentials : credentialList) {
				if (credentials.hasWriteAccess()) {
					return credentials;
				}
			}
		}
		return null;
	}

	private File getFile(TcServer server, String filename) {
		return (filename != null) ? new File(TcServer.substitute(filename, properties)) : null;
	}

	public String getHost() {
		String value = listener.getAttributeValue("bind");
		return (value != null) ? TcServer.substitute(value, properties) : null;
	}

	public String getPasswordFile() {
		return listener.getAttributeValue("passwordFile");
	}

	public String getPort() {
		String value = listener.getAttributeValue("port");
		return (value != null) ? TcServer.substitute(value, properties) : null;
	}

	public boolean isAuthenticationRequired() {
		return Boolean.valueOf(listener.getAttributeValue("accessFile"));
	}

	public boolean isSslRequired() {
		return Boolean.valueOf(listener.getAttributeValue("useSSL"));
	}

	public boolean isValid() {
		if (getHost() != null && getPort() != null) {
			try {
				Integer.parseInt(getPort());
				return true;
			}
			catch (NumberFormatException e) {
				// ignore
			}
		}
		return false;
	}

	private Map<String, String> read(File file) {
		try {
			BufferedReader reader = new BufferedReader(new FileReader(file));
			try {
				Map<String, String> map = new LinkedHashMap<String, String>();
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (!line.startsWith("#")) {
						String[] data = line.split("\\s+", 2);
						if (data.length == 2) {
							map.put(data[0], data[1]);
						}
					}
				}
				return map;
			}
			finally {
				reader.close();
			}
		}
		catch (IOException e) {
			TomcatPlugin.log(new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID, NLS.bind(
					"Failed to read file ''{0}''", file.getAbsoluteFile()), e));
		}
		return Collections.emptyMap();
	}
}
