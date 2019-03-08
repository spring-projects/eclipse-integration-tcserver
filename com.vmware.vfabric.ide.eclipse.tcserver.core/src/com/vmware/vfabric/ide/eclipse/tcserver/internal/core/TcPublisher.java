/*******************************************************************************
 * Copyright (c) 2012, 2014 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeoutException;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.tomcat.core.internal.PublishOperation2;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServerBehaviour;
import org.eclipse.jst.server.tomcat.core.internal.WebModule;
import org.eclipse.jst.server.tomcat.core.internal.xml.server40.ServerInstance;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.model.IModuleFile;
import org.eclipse.wst.server.core.model.IModuleFolder;
import org.eclipse.wst.server.core.model.IModuleResource;
import org.eclipse.wst.server.core.model.IModuleResourceDelta;
import org.eclipse.wst.server.core.model.PublishOperation;
import org.eclipse.wst.server.core.model.ServerBehaviourDelegate;
import org.eclipse.wst.server.core.util.PublishHelper;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.contentmodel.util.NamespaceTable;
import org.eclipse.wst.xml.core.internal.document.DOMModelImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.springframework.util.AntPathMatcher;

/**
 * Publishes and reloads modules through JMX. Only modules that have auto reload
 * set to false in the tc Server configuration are reloaded. If a server does
 * not have a local configuration the module is always re-deployed using a war
 * file.
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Leo Dos Santos
 */
public class TcPublisher extends PublishOperation2 {

	public static final String DEFAULT_NAMESPACE_URI = "http://www.springframework.org/schema/beans";

	private final IModule module2;

	public TcPublisher(TomcatServerBehaviour server, int kind, IModule[] module, int deltaKind) {
		super(server, kind, module, deltaKind);
		module2 = module[0];
	}

	/**
	 * @see PublishOperation#execute(IProgressMonitor, IAdaptable)
	 */
	@Override
	public void execute(IProgressMonitor monitor, IAdaptable info) throws CoreException {
		if (!((TcServer) server.getTomcatServer()).isEnhancedRedeployEnabled() || isModuleAutoReloadEnabled()) {
			// server will reload applications
			return;
		}

		// determine if module needs to be reloaded
		DeployInfo deployer = new DeployInfo(server, module);

		if (deltaKind == ServerBehaviourDelegate.REMOVED || server.getTomcatServer().isServeModulesWithoutPublish()) {
			if (!deployer.isLocal()) {
				TcUndeployModuleCommand command = new TcUndeployModuleCommand(deployer.getTcServerBehaviour(),
						deployer.getService(), deployer.getHost(), deployer.getContextPath());
				try {
					command.execute();
				}
				catch (TimeoutException e) {
					throw new CoreException(new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID, NLS.bind(
							"Timeout while publishing module ''{0}''", module2.getName())));
				}
				catch (CoreException e) {
					throw new CoreException(new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID, NLS.bind(
							"Failed to undeploy module ''{0}''", module2.getName())));
				}
			}

			// no reload necessary
			return;
		}

		if (kind == IServer.PUBLISH_CLEAN || kind == IServer.PUBLISH_FULL) {
			// force reload
			reload(deployer, monitor);
			return;
		}

		if (!deployer.isLocal()) {
			// force redeploy since files can not be copied locally
			reload(deployer, monitor);
			return;
		}

		Set<IModuleFile> files = new HashSet<IModuleFile>();
		IModuleResourceDelta[] delta = ((TcServerBehaviour) server).getPublishedResourceDelta(module);
		int size = delta.length;
		for (int i = 0; i < size; i++) {
			if (!onlyStaticResources(delta[i], files)) {
				// a dynamic resource has changed, reload app
				reload(deployer, monitor);
				return;
			}
		}
	}

	/**
	 * Checks if the given <code>file</code> is a root node that is a known
	 * Spring namespace.
	 */
	private boolean checkIfSpringConfigurationFile(IFile file) {
		IStructuredModel model = null;
		try {
			model = StructuredModelManager.getModelManager().getExistingModelForRead(file);
			if (model == null) {
				model = StructuredModelManager.getModelManager().getModelForRead(file);
			}
			if (model != null) {
				IDOMDocument document = ((DOMModelImpl) model).getDocument();
				if (document != null && document.getDocumentElement() != null) {
					String namespaceUri = document.getDocumentElement().getNamespaceURI();
					NamespaceTable table = new NamespaceTable(document);
					if (DEFAULT_NAMESPACE_URI.equals(namespaceUri)
							|| table.getNamespaceInfoForURI(DEFAULT_NAMESPACE_URI) != null) {
						return false;
					}
				}
			}
		}
		catch (Exception e) {
		}
		finally {
			if (model != null) {
				model.releaseFromRead();
			}
			model = null;
		}
		return true;
	}

	private boolean isModuleAutoReloadEnabled() {
		TcServer tcServer = (TcServer) server.getServer().loadAdapter(TcServer.class, null);
		IModule module2 = module[0];
		try {
			ServerInstance serverInstance = tcServer.getTomcatConfiguration().getServerInstance();
			if (serverInstance != null) {
				WebModule webModule = tcServer.getTomcatConfiguration().getWebModule(module2);
				return webModule != null && webModule.isReloadable();
			}
		}
		catch (CoreException e) {
			// ignore
		}
		return false;
	}

	/**
	 * Check if resource delta only contains static resources
	 */
	private boolean onlyStaticResources(IModuleResourceDelta delta, Set<IModuleFile> files) {
		if (delta.getModuleResource() instanceof IModuleFolder) {
			for (IModuleResourceDelta child : delta.getAffectedChildren()) {
				if (!onlyStaticResources(child, files)) {
					return false;
				}
			}
			return true;
		}
		else {
			if (delta.getModuleResource() instanceof IModuleFile) {
				files.add((IModuleFile) delta.getModuleResource());
			}
			String name = delta.getModuleResource().getName();

			// make that configurable
			if (name.endsWith(".xml")) {
				IFile file = (IFile) delta.getModuleResource().getAdapter(IFile.class);
				// check for spring context xml files first but exclude
				if (!checkIfSpringConfigurationFile(file)) {
					return false;
				}
			}
			boolean isStatic = false;
			// Check the configuration options for static resources
			AntPathMatcher matcher = new AntPathMatcher();
			TcServer tcServer = (TcServer) server.getServer().loadAdapter(TcServer.class, null);
			for (String pattern : StringUtils.splitByWholeSeparator(tcServer.getStaticFilenamePatterns(), ",")) {
				if (pattern.startsWith("!") && matcher.match(pattern.substring(1), name)) {
					isStatic = false;
				}
				else if (matcher.match(pattern, name)) {
					isStatic = true;
				}
			}
			return isStatic;
		}
	}

	private void reload(DeployInfo deployer, IProgressMonitor monitor) throws CoreException {
		if (server.getServer().getServerState() != IServer.STATE_STARTED) {
			return;
		}

		try {
			String deployPath;
			if (deployer.isLocal()) {
				deployPath = null;
			}
			else {
				// server has no local configuration, re-deploy application
				// deploy application to war file for upload
				IPath path = deployer.getTcServerBehaviour().getServerDeployDirectory()
						.append(deployer.getContextPath() + ".war");
				IPath base = server.getRuntimeBaseDirectory();
				PublishHelper helper = new PublishHelper(base.append("temp").toFile());
				IModuleResource[] mr = ((TcServerBehaviour) server).getResources(module);
				IStatus[] stat = helper.publishZip(mr, path, monitor);
				if (stat.length > 0) {
					throw new CoreException(new MultiStatus(ITcServerConstants.PLUGIN_ID, 0, stat, NLS.bind(
							"Failed to gather resources to publish module ''{0}''", module2.getName()), null));
				}
				deployPath = deployer.getTcServerBehaviour().getDeployRoot() + path.lastSegment();
			}

			TcReloadModuleCommand command = new TcReloadModuleCommand(deployer.getTcServerBehaviour(),
					deployer.getService(), deployer.getHost(), deployer.getContextPath(), deployPath);
			command.setForceDeploy(deployPath != null);
			command.execute();
		}
		catch (TimeoutException e) {
			throw new CoreException(new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID, NLS.bind(
					"Timeout while publishing module ''{0}''", module2.getName())));
		}
		catch (CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID, NLS.bind(
					"Failed to publish module ''{0}''", module2.getName())));
		}
	}

}
