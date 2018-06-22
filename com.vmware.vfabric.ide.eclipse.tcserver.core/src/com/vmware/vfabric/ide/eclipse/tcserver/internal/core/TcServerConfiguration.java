/*******************************************************************************
 * Copyright (c) 2012, 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.tomcat.core.internal.Messages;
import org.eclipse.jst.server.tomcat.core.internal.ProgressUtil;
import org.eclipse.jst.server.tomcat.core.internal.Tomcat60Configuration;
import org.eclipse.jst.server.tomcat.core.internal.TomcatVersionHelper;
import org.eclipse.jst.server.tomcat.core.internal.Trace;
import org.eclipse.jst.server.tomcat.core.internal.WebModule;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.Connector;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.Listener;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.ServerInstance;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.Service;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.ServerPort;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class TcServerConfiguration extends Tomcat60Configuration {

	public static final String MODIFY_SERVER_PROPERTY_PROPERTY = "modifyServerProperty";

	private static final String FILE_JMXREMOTE_ACCESS = "jmxremote.access";

	private static final String FILE_JMXREMOTE_PASSWORD = "jmxremote.password";

	private static final String FILE_SPRING_INSIGHT = "spring-insight.yml";

	private static final String JMX_SOCKET_LISTENER_CLASS = "com.springsource.tcserver.serviceability.rmi.JmxSocketListener";

	private String jmxAccessFile;

	private String jmxPasswordFile;

	private Properties properties = new Properties();

	private String springInsightFile;

	private final boolean supportsServlet30;

	private final TcServer tcServer;

	public TcServerConfiguration(TcServer server, IFolder folder) {
		this(server, folder, false);
	}

	public TcServerConfiguration(TcServer server, IFolder folder, boolean supportsServlet30) {
		super(folder);
		this.supportsServlet30 = supportsServlet30;
		this.tcServer = server;
	}

	public Listener[] getListeners() {
		return serverInstance.getListeners();
	}

	public List<ServerProperty> getProperties() {
		List<ServerProperty> serverProperties = new ArrayList<ServerProperty>();
		if (properties != null) {
			for (Entry<Object, Object> entry : properties.entrySet()) {
				serverProperties.add(new ServerProperty((String) entry.getKey(), (String) entry.getValue()));
			}
		}
		return serverProperties;
	}

	public ServerInstance getServerInstance() {
		return serverInstance;
	}

	@SuppressWarnings({ "rawtypes" })
	@Override
	public List getServerPorts() {
		List ports = getTcServerPorts();
		for (Iterator it = ports.iterator(); it.hasNext();) {
			ServerPort port = (ServerPort) it.next();
			// remove shutdown port from list to avoid error when starting
			// server
			if (port.getPort() == -1) {
				it.remove();
			}
		}
		return ports;
	}
	
	@Override
	public ServerPort getMainPort() {
		ServerPort mainTomcatPort = super.getMainPort();
		// AJP, APR profiles for tc Server won't have explicit main port, therefore assume it's 8080 by default
		return mainTomcatPort == null ? new ServerPort("0", "Default", 8080, "HTTP") : mainTomcatPort;
	}

	public JmxServicabilityInfo getServicabilityInfo(IPath basePath) {
		// add base path property
		String path = basePath.toFile().getAbsolutePath();
		properties.setProperty("catalina.base", path);

		for (Listener listener : getListeners()) {
			if (JMX_SOCKET_LISTENER_CLASS.equals(listener.getClassName())) {
				return new JmxServicabilityInfo(listener, properties);
			}
		}
		return null;
	}

	public int getShutdownPort() {
		List<TcServerPort> ports = getTcServerPorts();
		for (TcServerPort port : ports) {
			if ("server".equals(port.getId())) {
				return port.getPort();
			}
		}
		return -1;
	}

	public List<TcServerPort> getTcServerPorts() {
		List<TcServerPort> ports = new ArrayList<TcServerPort>();

		// first add server port
		try {
			String portString = server.getPort();
			int port = getPort(portString);
			TcServerPort serverPort = new TcServerPort("server", Messages.portServer, port, "TCPIP");
			serverPort.setPortString(portString);
			ports.add(serverPort);
		}
		catch (Exception e) {
			// ignore
		}

		// add connectors
		try {
			String instanceServiceName = serverInstance.getService().getName();
			int size = server.getServiceCount();
			for (int i = 0; i < size; i++) {
				Service service = server.getService(i);
				int size2 = service.getConnectorCount();
				for (int j = 0; j < size2; j++) {
					Connector connector = service.getConnector(j);
					String name = "HTTP/1.1";
					String protocol2 = "HTTP";
					boolean advanced = true;
					String[] contentTypes = null;
					String portString = connector.getPort();
					int port = getPort(portString);
					String protocol = connector.getProtocol();
					if (protocol != null && protocol.length() > 0) {
						if (protocol.startsWith("HTTP")) {
							name = protocol;
						}
						else if (protocol.startsWith("AJP")) {
							name = protocol;
							protocol2 = "AJP";
						}
						else {
							// Get Tomcat equivalent name if protocol handler
							// class specified
							name = (String) protocolHandlerMap.get(protocol);
							if (name != null) {
								// Prepare simple protocol string for ServerPort
								// protocol
								int index = name.indexOf('/');
								if (index > 0) {
									protocol2 = name.substring(0, index);
								}
								else {
									protocol2 = name;
								}
							}
							// Specified protocol is unknown, just use as is
							else {
								name = protocol;
								protocol2 = protocol;
							}
						}
					}
					if (protocol2.toLowerCase().equals("http")) {
						contentTypes = new String[] { "web", "webservices" };
					}
					String secure = connector.getSecure();
					if (secure != null && secure.length() > 0) {
						name = "SSL";
						protocol2 = "SSL";
					}
					else {
						advanced = false;
					}
					String portId;
					if (instanceServiceName != null && instanceServiceName.equals(service.getName())) {
						portId = Integer.toString(j);
					}
					else {
						portId = i + "/" + j;
					}
					TcServerPort serverPort = new TcServerPort(portId, name, port, protocol2, contentTypes, advanced);
					serverPort.setPortString(portString);
					ports.add(serverPort);
				}
			}
		}
		catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error getting server ports", e);
		}
		return ports;
	}

	@Override
	public void load(IFolder folder, IProgressMonitor monitor) throws CoreException {
		super.load(folder, monitor);

		properties = new Properties();
		if (propertiesFile != null) {
			loadProperties(new ByteArrayInputStream(propertiesFile.getBytes()));
		}

		try {
			IFile file = folder.getFile(FILE_JMXREMOTE_ACCESS);
			if (file.exists()) {
				jmxAccessFile = TomcatVersionHelper.getFileContents(file.getContents());
			}
			else {
				jmxAccessFile = null;
			}

			file = folder.getFile(FILE_JMXREMOTE_PASSWORD);
			if (file.exists()) {
				jmxPasswordFile = TomcatVersionHelper.getFileContents(file.getContents());
			}
			else {
				jmxPasswordFile = null;
			}

			file = folder.getFile(FILE_SPRING_INSIGHT);
			if (file.exists()) {
				springInsightFile = TomcatVersionHelper.getFileContents(file.getContents());
			}
			else {
				springInsightFile = null;
			}
		}
		catch (Exception e) {
			Trace.trace(Trace.WARNING, "Could not load tc Server configuration from: " + folder.getFullPath() + ": "
					+ e.getMessage());
		}
	}

	@Override
	public void load(IPath path, IProgressMonitor monitor) throws CoreException {
		super.load(path, monitor);

		properties = new Properties();
		if (propertiesFile != null) {
			loadProperties(new ByteArrayInputStream(propertiesFile.getBytes()));
		}

		try {
			File file = path.append(FILE_JMXREMOTE_ACCESS).toFile();
			if (file.exists()) {
				jmxAccessFile = TomcatVersionHelper.getFileContents(new FileInputStream(file));
			}
			else {
				jmxAccessFile = null;
			}

			file = path.append(FILE_JMXREMOTE_PASSWORD).toFile();
			if (file.exists()) {
				jmxPasswordFile = TomcatVersionHelper.getFileContents(new FileInputStream(file));
			}
			else {
				jmxPasswordFile = null;
			}

			file = path.append(FILE_SPRING_INSIGHT).toFile();
			if (file.exists()) {
				springInsightFile = TomcatVersionHelper.getFileContents(new FileInputStream(file));
			}
			else {
				springInsightFile = null;
			}
		}
		catch (Exception e) {
			Trace.trace(Trace.WARNING,
					"Could not load tc Server configuration from: " + path.toOSString() + ": " + e.getMessage());
		}
	}

	/**
	 * Loads catalina.properties to process place holders in configuration
	 * files.
	 */
	public void loadProperties(InputStream in) {
		// load properties from configuration
		try {
			properties.load(in);
		}
		catch (IOException e) {
			// ignore
		}
	}

	public void modifyProperty(String key, String value) {
		Map<String, String> values = new HashMap<String, String>();
		values.put(key, value);
		PropertyWriter writer = new PropertyWriter(values);
		try {
			propertiesFile = writer.apply((propertiesFile != null) ? propertiesFile : "");
		}
		catch (IOException e) {
			Trace.trace(Trace.SEVERE, "Could not read properties", e);
		}
		properties.put(key, value);
		firePropertyChangeEvent(MODIFY_SERVER_PROPERTY_PROPERTY, key, value);
	}

	public void modifyServerPort(String id, String portString) {
		int port = getPort(portString);
		try {
			if ("server".equals(id)) {
				server.setPort(portString);
				isServerDirty = true;
				firePropertyChangeEvent(MODIFY_PORT_PROPERTY, id, new Integer(port));
				return;
			}

			int i = id.indexOf("/");
			// If a connector in the instance Service
			if (i < 0) {
				int connNum = Integer.parseInt(id);
				Connector connector = serverInstance.getConnector(connNum);
				if (connector != null) {
					connector.setPort(portString);
					isServerDirty = true;
					firePropertyChangeEvent(MODIFY_PORT_PROPERTY, id, new Integer(port));
				}
			}
			// Else a connector in another Service
			else {
				int servNum = Integer.parseInt(id.substring(0, i));
				int connNum = Integer.parseInt(id.substring(i + 1));

				Service service = server.getService(servNum);
				Connector connector = service.getConnector(connNum);
				connector.setPort(portString);
				isServerDirty = true;
				firePropertyChangeEvent(MODIFY_PORT_PROPERTY, id, new Integer(port));
			}
		}
		catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Error modifying server port " + id, e);
		}
	}

	@Override
	public void save(IFolder folder, IProgressMonitor monitor) throws CoreException {
		checkModuleReloadState();
		super.save(folder, monitor);

		try {
			// save catalina.properties
			if (propertiesFile != null) {
				ByteArrayInputStream in = new ByteArrayInputStream(propertiesFile.getBytes());
				IFile file = folder.getFile("catalina.properties");
				if (file.exists()) {
					file.setContents(in, true, true, ProgressUtil.getSubMonitorFor(monitor, 200));
				}
				else {
					file.create(in, true, ProgressUtil.getSubMonitorFor(monitor, 200));
				}
			}

			if (jmxAccessFile != null) {
				ByteArrayInputStream in = new ByteArrayInputStream(jmxAccessFile.getBytes());
				IFile file = folder.getFile(FILE_JMXREMOTE_ACCESS);
				if (!file.exists()) {
					file.create(in, true, ProgressUtil.getSubMonitorFor(monitor, 200));
				}
			}
			if (jmxPasswordFile != null) {
				ByteArrayInputStream in = new ByteArrayInputStream(jmxPasswordFile.getBytes());
				IFile file = folder.getFile(FILE_JMXREMOTE_PASSWORD);
				if (!file.exists()) {
					file.create(in, true, ProgressUtil.getSubMonitorFor(monitor, 200));
				}
			}
			if (springInsightFile != null) {
				ByteArrayInputStream in = new ByteArrayInputStream(springInsightFile.getBytes());
				IFile file = folder.getFile(FILE_SPRING_INSIGHT);
				if (!file.exists()) {
					file.create(in, true, ProgressUtil.getSubMonitorFor(monitor, 200));
				}
			}
		}
		catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Could not save tc Server configuration to " + folder.toString(), e);
		}
	}

	@Override
	public void save(IPath path, IProgressMonitor monitor) throws CoreException {
		checkModuleReloadState();
		super.save(path, monitor);
	}

	@SuppressWarnings("unchecked")
	private void checkModuleReloadState() {
		// Make sure modules are not reloadable if we use JMX or agent-based
		// reloading
		if (tcServer.isAgentRedeployEnabled() || tcServer.isEnhancedRedeployEnabled()) {
			List<WebModule> modules = new ArrayList<WebModule>(getWebModules());
			for (int i = 0; i < modules.size(); i++) {
				WebModule module = modules.get(i);
				if (module.isReloadable()) {
					modifyWebModule(i, module.getDocumentBase(), module.getPath(), false);
				}
			}
		}
	}

	private int getPort(String portString) {
		int port = -1;
		try {
			port = Integer.parseInt(TcServer.substitute(portString, properties));
		}
		catch (Exception e) {
			// ignore
		}
		return port;
	}

	@Override
	protected IStatus backupAndPublish(IPath tomcatDir, boolean doBackup, IProgressMonitor monitor) {
		return super.backupAndPublish(tomcatDir, doBackup, monitor);
	}

	@Override
	protected IStatus cleanupServer(IPath baseDir, IPath installDir, boolean removeKeptContextFiles,
			IProgressMonitor monitor) {
		return super.cleanupServer(baseDir, installDir, removeKeptContextFiles, monitor);
	}

	@Override
	protected void save(IPath path, boolean forceDirty, IProgressMonitor monitor) throws CoreException {
		super.save(path, forceDirty, monitor);

		try {
			if (jmxAccessFile != null && forceDirty) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(path.append(FILE_JMXREMOTE_ACCESS).toFile()));
				bw.write(jmxAccessFile);
				bw.close();
			}
			if (jmxPasswordFile != null && forceDirty) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(path.append(FILE_JMXREMOTE_PASSWORD).toFile()));
				bw.write(jmxPasswordFile);
				bw.close();
			}
			if (springInsightFile != null && forceDirty) {
				BufferedWriter bw = new BufferedWriter(new FileWriter(path.append(FILE_SPRING_INSIGHT).toFile()));
				bw.write(springInsightFile);
				bw.close();
			}
		}
		catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Could not save Tomcat configuration to " + path, e);
		}
	}

	// TODO e3.6 remove
	protected IStatus updateContextsToServeDirectly(IPath baseDir, String loader, IProgressMonitor monitor) {
		try {
			// Eclipse 3.6
			Method method = TomcatVersionHelper.class.getDeclaredMethod("updateContextsToServeDirectly", IPath.class,
					String.class, Boolean.class, IProgressMonitor.class);
			return (IStatus) method.invoke(null, baseDir, loader, supportsServlet30, monitor);
		}
		catch (NoSuchMethodException ignore) {
			try {
				// Eclipse 3.5
				Method method = TomcatVersionHelper.class.getDeclaredMethod("updateContextsToServeDirectly",
						IPath.class, String.class, IProgressMonitor.class);
				return (IStatus) method.invoke(null, baseDir, loader, monitor);
			}
			catch (Exception e) {
				return new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID,
						"Internal error while updating contexts", e);
			}
		}
		catch (Exception e) {
			return new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID, "Internal error while updating contexts", e);
		}
	}

	public ServerPort getMainSslPort() {
		Iterator<?> iterator = getServerPorts().iterator();
		while (iterator.hasNext()) {
			ServerPort port = (ServerPort) iterator.next();
			if (port.getProtocol().toLowerCase().equals("ssl") && port.getId().indexOf('/') < 0) {
				return port;
			}
		}
		return null;
	}

	/*
	 * Make super method package visible.
	 */
	@Override
	protected String getWebModuleURL(IModule webModule) {
		return super.getWebModuleURL(webModule);
	}

}
