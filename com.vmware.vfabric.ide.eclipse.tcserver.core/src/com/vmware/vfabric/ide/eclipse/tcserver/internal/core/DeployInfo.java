/*******************************************************************************
 * Copyright (c) 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.server.core.IWebModule;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServerBehaviour;
import org.eclipse.jst.server.tomcat.core.internal.WebModule;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.ServerInstance;
import org.eclipse.wst.server.core.IModule;

/**
 * Module deployment info. Ised by various server JMX commands.
 *
 * @author Alex Boyko
 *
 */
public class DeployInfo {

	private final String service;

	private final String host;

	private final String contextPath;

	private final TcServer tcServer;

	private final TcServerBehaviour tcServerBehaviour;

	private boolean local;

	public DeployInfo(TomcatServerBehaviour server, IModule[] module) throws CoreException {
		tcServer = (TcServer) server.getServer().loadAdapter(TcServer.class, null);
		tcServerBehaviour = (TcServerBehaviour) server.getServer().loadAdapter(TcServerBehaviour.class, null);
		ServerInstance serverInstance = tcServer.getTomcatConfiguration().getServerInstance();
		if (serverInstance != null) {
			local = true;
			service = serverInstance.getService().getName();
			host = serverInstance.getHost().getName();
			WebModule webModule = tcServer.getTomcatConfiguration().getWebModule(module[0]);
			if (webModule != null) {
				contextPath = webModule.getPath();
			}
			else {
				contextPath = computeContextPath(module[0]);
			}
		}
		else {
			// server has no local configuration, re-deploy application
			local = false;
			service = tcServer.getDeployerService();
			host = tcServer.getDeployerHost();
			contextPath = computeContextPath(module[0]);
		}
	}

	public String getContextPath() {
		return contextPath;
	}

	public String getHost() {
		return host;
	}

	public String getService() {
		return service;
	}

	public TcServer getTcServer() {
		return tcServer;
	}

	public TcServerBehaviour getTcServerBehaviour() {
		return tcServerBehaviour;
	}

	public boolean isLocal() {
		return local;
	}

	private String computeContextPath(IModule module) {
		IWebModule webModule = (IWebModule) module.loadAdapter(IWebModule.class, null);
		if (webModule != null) {
			String contextRoot = webModule.getContextRoot();
			if (contextRoot != null && contextRoot.length() > 0) {
				return !contextRoot.startsWith("/") ? "/" + contextRoot : contextRoot;
			}
		}
		return "/" + module.getName();
	}

}
