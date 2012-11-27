/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.tomcat.core.internal.ITomcatServer;
import org.eclipse.jst.server.tomcat.core.internal.Messages;
import org.eclipse.jst.server.tomcat.core.internal.TomcatPlugin;
import org.eclipse.jst.server.tomcat.core.internal.TomcatServer;
import org.eclipse.jst.server.tomcat.core.internal.Trace;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IRuntime;
import org.eclipse.wst.server.core.ServerPort;
import org.eclipse.wst.server.core.ServerUtil;

/**
 * @author Steffen Pingel
 * @author Christian Dupuis
 * @author Leo Dos Santos
 * @author Tomasz Zarna
 */
public class TcServer extends TomcatServer {

	public static String ID_TC_SERVER_2_0 = "com.springsource.tcserver.60";

	public static String ID_TC_SERVER_2_1 = "com.springsource.tcserver.70";

	public static String ID_TC_SERVER_2_5 = "com.vmware.server.tc.70";

	public static final String DEFAULT_DEPLOYER_HOST = "localhost";

	public static final String DEFAULT_DEPLOYER_SERVICE = "Catalina";

	/**
	 * Default filename patters that should to avoid a webapp reload when
	 * publishing.
	 */
	public static final String DEFAULT_STATIC_FILENAMES = "*.html,*.xhtml,*.css,*.jspx,*.js,*.jsp,*.gif,*.jpg,*.png,*.swf,*-flow.xml,*.properties,*.xml,!tiles.xml,!web.xml";

	/** Boolean property that determines if the ASF layout should be used. */
	public static final String KEY_ASF_LAYOUT = "com.springsource.tcserver.asf";

	/**
	 * String property for the server instance for combined or separate layout.
	 */
	public static final String KEY_SERVER_NAME = "com.springsource.tcserver.name";

	public static final String PROPERTY_ADD_EXTRA_VMARGS = "addExtraVmArgs";

	public static final String PROPERTY_AGENT_OPTIONS = "com.springsource.tcserver.agent.options";

	public static final String PROPERTY_AGENT_REDEPLOY = "com.springsource.tcserver.agent.deploy";

	public static final String PROPERTY_DEPLOYER_HOST = "modifyDeployerHost";

	public static final String PROPERTY_DEPLOYER_SERVICE = "modifyDeployerService";

	/**
	 * Property key for a boolean that indicates managing of webapp reloading is
	 * enabled.
	 */
	public static final String PROPERTY_ENHANCED_REDEPLOY = "com.springsource.tcserver.jmx.deploy";

	public static final String PROPERTY_JMX_PASSWORD = "modifyJmxPassowrd";

	public static final String PROPERTY_JMX_PORT = "modifyJmxPort";

	public static final String PROPERTY_JMX_USER = "modifyJmxUser";

	public static final String PROPERTY_REMOVE_EXTRA_VMARGS = "removeExtraVmArgs";

	/**
	 * Property key for list of patterns to avoid a webapp reload when
	 * publishing.
	 */
	public static final String PROPERTY_STATIC_FILENAMES = "com.springsource.tcserver.filenames.static";

	private static TcServerCallback callback;

	private static final String DEFAULT_JMX_PORT = "6969";

	@Override
	public IStatus canModifyModules(IModule[] add, IModule[] remove) {
		if (add != null) {
			int size = add.length;
			for (int i = 0; i < size; i++) {
				IModule module = add[i];

				if (ITcServerConstants.GRAILS_APP_MODULE_TYPE.equals(module.getModuleType().getId())) {
					return Status.OK_STATUS;
				}
			}
		}

		return super.canModifyModules(add, remove);
	}

	@SuppressWarnings("unchecked")
	public List<String> getAddExtraVmArgs() {
		return getAttribute(PROPERTY_ADD_EXTRA_VMARGS, Collections.EMPTY_LIST);
	}

	public String getAgentOptions() {
		return getAttribute(PROPERTY_AGENT_OPTIONS, "");
	}

	public String getDeployerHost() {
		return getAttribute(PROPERTY_DEPLOYER_HOST, DEFAULT_DEPLOYER_HOST);
	}

	public String getDeployerProperty(String key) {
		if (PROPERTY_JMX_PORT.equals(key)) {
			return getJmxPort();
		}
		if (PROPERTY_DEPLOYER_HOST.equals(key)) {
			return getDeployerHost();
		}
		if (PROPERTY_DEPLOYER_SERVICE.equals(key)) {
			return getDeployerService();
		}
		return getAttribute(key, (String) null);
	}

	public String getDeployerService() {
		return getAttribute(PROPERTY_DEPLOYER_SERVICE, DEFAULT_DEPLOYER_SERVICE);
	}

	/**
	 * The runtime may specifies the top-level tc Server directory and not the
	 * catalina home. This methods returns the actual catalina home this server
	 * is configured to use.
	 */
	public IPath getInstanceBase(IRuntime runtime) {
		IPath path = runtime.getLocation();
		if (isAsfLayout()) {
			return getTomcatRuntime().getTomcatLocation();
		}
		else {
			String instanceDir = getInstanceDirectory();
			if (instanceDir != null) {
				path = Path.fromOSString(instanceDir);
			}
			String serverName = getAttribute(KEY_SERVER_NAME, (String) null);
			if (serverName != null) {
				path = path.append(serverName);
			}
			return path;
		}
	}

	public String getJmxPassword() {
		return getAttribute(PROPERTY_JMX_PASSWORD, "");
	}

	public String getJmxPort() {
		return getAttribute(PROPERTY_JMX_PORT, DEFAULT_JMX_PORT);
	}

	public String getJmxUser() {
		return getAttribute(PROPERTY_JMX_USER, "");
	}

	public Layout getLayout() {
		if (isAsfLayout()) {
			return Layout.ASF;
		}
		else {
			IPath path = getInstanceBase(getServer().getRuntime());
			if (path.append("lib").append("catalina.jar").toFile().exists()) {
				return Layout.COMBINED;
			}
			else {
				return Layout.SEPARATE;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public List<String> getRemoveExtraVmArgs() {
		return getAttribute(PROPERTY_REMOVE_EXTRA_VMARGS, Collections.EMPTY_LIST);
	}

	@Override
	public IModule[] getRootModules(IModule module) throws CoreException {
		if (ITcServerConstants.GRAILS_APP_MODULE_TYPE.equals(module.getModuleType().getId())) {
			return new IModule[] { module };
		}
		return super.getRootModules(module);
	}

	public String getServerName() {
		return getAttribute(TcServer.KEY_SERVER_NAME, (String) null);
	}

	public String getStaticFilenamePatterns() {
		return getAttribute(PROPERTY_STATIC_FILENAMES, DEFAULT_STATIC_FILENAMES);
	}

	@Override
	public TcServerConfiguration getTomcatConfiguration() throws CoreException {
		if (configuration == null) {
			IFolder folder = getFolder();
			configuration = new TcServerConfiguration(this, folder, getTomcatRuntime().supportsServlet30());
			try {
				((TcServerConfiguration) configuration).load(folder, null);
			}
			catch (CoreException ce) {
				// ignore
				configuration = null;
				throw ce;
			}
		}
		return (TcServerConfiguration) configuration;
	}

	@Override
	public TcServerRuntime getTomcatRuntime() {
		return (TcServerRuntime) super.getTomcatRuntime();
	}

	@Override
	public void importRuntimeConfiguration(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
		try {
			importRuntimeConfigurationChecked(runtime, monitor);
		}
		catch (CoreException ce) {
			// ignore, need additional configuration for server instance
			// Webtools invokes importRuntimeConfiguration() before any
			// configuration has taken place therefore this method need to fail
			// silently and the configuration needs to be imported again later
			// on
			configuration = null;
		}
	}

	public void importRuntimeConfigurationChecked(IRuntime runtime, IProgressMonitor monitor) throws CoreException {
		if (runtime == null) {
			configuration = null;
			return;
		}
		IPath path = getInstanceBase(runtime);
		path = path.append("conf");
		IFolder folder = getServer().getServerConfiguration();
		configuration = new TcServerConfiguration(this, folder);
		configuration.importFromPath(path, isTestEnvironment(), monitor);
	}

	public boolean isAgentRedeployEnabled() {
		return getAttribute(PROPERTY_AGENT_REDEPLOY, false);
	}

	public boolean isAsfLayout() {
		return getAttribute(TcServer.KEY_ASF_LAYOUT, true);
	}

	public boolean isEnhancedRedeployEnabled() {
		return getAttribute(PROPERTY_ENHANCED_REDEPLOY, false);
	}

	public void setAddExtraVmArgs(List<String> value) {
		setAttribute(PROPERTY_ADD_EXTRA_VMARGS, value);
	}

	public void setAgentOptions(String agentOptions) {
		setAttribute(PROPERTY_AGENT_OPTIONS, agentOptions);
	}

	public void setAgentRedeployEnabled(boolean enable) {
		setAttribute(PROPERTY_AGENT_REDEPLOY, enable);
	}

	@Override
	public void setDefaults(IProgressMonitor monitor) {
		super.setDefaults(monitor);
		// test mode is now supported
		// setAttribute(ITomcatServer.PROPERTY_INSTANCE_DIR, (String) null);
		// setAttribute(ITomcatServer.PROPERTY_TEST_ENVIRONMENT, false);
		// ASF layout is only supported by tc Server 2.0 and earlier
		if (isVersion25(getServer().getRuntime())) {
			setAttribute(TcServer.KEY_ASF_LAYOUT, false);
			setAttribute(ITomcatServer.PROPERTY_SAVE_SEPARATE_CONTEXT_FILES, true);
		}
		else {
			setAttribute(TcServer.KEY_ASF_LAYOUT, true);

		}
		getCallback().setDefaults(this, monitor);
	}

	public void setDeployerHost(String value) {
		setAttribute(PROPERTY_DEPLOYER_HOST, value);
	}

	public void setDeployerProperty(String key, String value) {
		setAttribute(key, value);
	}

	public void setDeployerService(String value) {
		setAttribute(PROPERTY_DEPLOYER_SERVICE, value);
	}

	public void setEnhancedRedeployEnabled(boolean enable) {
		setAttribute(PROPERTY_ENHANCED_REDEPLOY, enable);
	}

	public void setJmxPassword(String value) {
		setAttribute(PROPERTY_JMX_PASSWORD, value);
	}

	public void setJmxPort(String value) {
		setAttribute(PROPERTY_JMX_PORT, value);
	}

	public void setJmxUser(String value) {
		setAttribute(PROPERTY_JMX_USER, value);
	}

	public void setRemoveExtraVmArgs(List<String> value) {
		setAttribute(PROPERTY_REMOVE_EXTRA_VMARGS, value);
	}

	public void setStaticFilenamePatterns(String filenamePatterns) {
		setAttribute(PROPERTY_STATIC_FILENAMES, filenamePatterns);
	}

	protected IFolder getFolder() throws CoreException {
		IFolder folder = getServer().getServerConfiguration();
		if (folder == null || !folder.exists()) {
			String path = null;
			if (folder != null) {
				path = folder.getFullPath().toOSString();
			}
			throw new CoreException(new Status(IStatus.ERROR, TomcatPlugin.PLUGIN_ID, 0, NLS.bind(
					Messages.errorNoConfiguration, path), null));
		}
		return folder;
	}

	@Override
	protected void initialize() {
		super.initialize();
	}

	public static synchronized TcServerCallback getCallback() {
		if (callback == null) {
			callback = ExtensionPointReader.readExtension();
			if (callback == null) {
				// create null callback
				callback = new TcServerCallback() {
					// ignore
				};
			}
		}
		return callback;
	}

	public static boolean isVersion25(IRuntime runtime) {
		return runtime.getRuntimeType().getId().endsWith("70");
	}

	public static String substitute(String value, Properties properties) {
		String[] segments = value.split("\\$\\{");
		StringBuffer sb = new StringBuffer(value.length());
		sb.append(segments[0]);
		for (int i = 1; i < segments.length; i++) {
			String segment = segments[i];
			String substitution = null;
			int brace = segment.indexOf('}');
			if (brace > 0) {
				String keyword = segment.substring(0, brace);
				substitution = properties.getProperty(keyword);
			}

			if (substitution != null) {
				sb.append(substitution);
				sb.append(segment.substring(brace + 1));
			}
			else {
				sb.append("${");
				sb.append(segment);
			}
		}
		return sb.toString();
	}

	public enum Layout {
		/**
		 * Uses runtime directory. Does not have an instance directory.
		 * Supported by v2.0 and earlier only.
		 */
		ASF,
		/** Uses instance directory. Supported by v2.5 and later only. */
		COMBINED,
		/** Uses runtime and instance directory. Supported by all versions. */
		SEPARATE;

		public String toString() {
			switch (this) {
			case ASF:
				return "ASF Layout";
			case COMBINED:
				return "Combined Layout";
			case SEPARATE:
				return "Separate Layout";
			}
			throw new IllegalStateException();
		};
	}

	private static class ExtensionPointReader {

		private static final String ELEMENT_CALLBACK = "callback";

		private static final String ELEMENT_CLASS = "class";

		private static final String EXTENSION_ID_CALLBACK = "com.springsource.sts.server.tc.core.callback";

		public static TcServerCallback readExtension() {
			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_ID_CALLBACK);
			IExtension[] extensions = extensionPoint.getExtensions();
			for (IExtension extension : extensions) {
				IConfigurationElement[] elements = extension.getConfigurationElements();
				for (IConfigurationElement element : elements) {
					if (element.getName().compareTo(ELEMENT_CALLBACK) == 0) {
						return readCallbackExtension(element);
					}
				}
			}
			return null;
		}

		private static TcServerCallback readCallbackExtension(IConfigurationElement configurationElement) {
			try {
				Object object = configurationElement.createExecutableExtension(ELEMENT_CLASS);
				if (!(object instanceof TcServerCallback)) {
					TomcatPlugin.log(new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID, "Could not load "
							+ object.getClass().getCanonicalName() + " must implement "
							+ TcServerCallback.class.getCanonicalName()));
					return null;
				}

				return (TcServerCallback) object;
			}
			catch (CoreException e) {
				TomcatPlugin.log(new Status(IStatus.ERROR, ITcServerConstants.PLUGIN_ID,
						"Could not load callback extension", e));
			}
			return null;
		}

	}

	@Override
	public URL getModuleRootURL(IModule module) {
		URL url = super.getModuleRootURL(module);
		if (url != null) {
			return url;
		}

		// if standard method fails, return URL for SSL connection
		try {
			if (module == null) {
				return null;
			}

			TcServerConfiguration config = getTomcatConfiguration();
			if (config == null) {
				return null;
			}

			ServerPort sslPort = config.getMainSslPort();
			if (sslPort == null) {
				return null;
			}

			int port = sslPort.getPort();
			String urlString = "https://" + getServer().getHost();
			port = ServerUtil.getMonitoredPort(getServer(), port, "web");
			if (port != 443) {
				urlString += ":" + port;
			}
			urlString += config.getWebModuleURL(module);
			if (!urlString.endsWith("/")) {
				urlString += "/";
			}
			return new URL(urlString);
		}
		catch (Exception e) {
			Trace.trace(Trace.SEVERE, "Could not get root URL", e);
			return null;
		}
	}

}
