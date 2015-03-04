/*******************************************************************************
 * Copyright (c) 2012 - 2015 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.wst.server.core.IServerAttributes;
import org.springsource.ide.eclipse.commons.core.FileUtil;

/**
 * @author Tomasz Zarna
 *
 */
public class TemplatePropertiesReader {

	private static final String CONFIGURATION_PROMPTS_PROPERTIES = "configuration-prompts.properties";

	private static final String SERVER_FRAGMENT_XML = "conf/server-fragment.xml";

	private static final String CONTEXT_FRAGMENT_XML = "conf/context-fragment.xml";

	private static final String SSL_PROPERTIES = "conf/ssl.properties";

	private final IServerAttributes serverAttributes;

	@SuppressWarnings("serial")
	static private class OrderedProperties extends Properties {

		private final Vector<String> names;

		public OrderedProperties() {
			super();
			names = new Vector<String>();
		}

		public Enumeration<String> propertyNames() {
			return names.elements();
		}

		public Object put(Object key, Object value) {
			if (names.contains(key)) {
				names.remove(key);
			}
			names.add((String) key);

			return super.put(key, value);
		}

		public Object remove(Object key) {
			names.remove(key);
			return super.remove(key);
		}
	}

	public TemplatePropertiesReader(IServerAttributes serverAttributes) {
		this.serverAttributes = serverAttributes;
	}

	public Set<TemplateProperty> read(String templateName, IProgressMonitor monitor) throws CoreException {
		TcServerRuntime tcRuntime = (TcServerRuntime) serverAttributes.getRuntime().loadAdapter(TcServerRuntime.class,
				monitor);
		if (tcRuntime != null) {
			File templateDir = tcRuntime.getTemplateFolder(templateName);
			if (templateDir.exists()) {
				return read(templateDir, monitor);
			}
		}
		return null;
	}

	private Set<TemplateProperty> read(File templateDir, IProgressMonitor monitor) throws CoreException {
		File configurationPromptsFile = new File(templateDir, CONFIGURATION_PROMPTS_PROPERTIES);
		Properties props = new TemplatePropertiesReader.OrderedProperties();
		if (!configurationPromptsFile.exists()) {
			return Collections.emptySet();
		}
		try {
			FileReader in = new FileReader(configurationPromptsFile);
			props.load(in);
		}
		catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, TcServerCorePlugin.PLUGIN_ID, "Could not read file \""
					+ configurationPromptsFile + "\"", e));
		}

		if (props.size() == 0) {
			return Collections.emptySet();
		}

		File fragmentXmlFile = new File(templateDir, SERVER_FRAGMENT_XML);
		if (!fragmentXmlFile.exists() || !fragmentXmlFile.isFile()) {
			fragmentXmlFile = new File(templateDir, CONTEXT_FRAGMENT_XML);
		}
		String serverFragmentContent = FileUtil.readFile(fragmentXmlFile, monitor);

		File sslPropertiesFile = new File(templateDir, SSL_PROPERTIES);
		String sslPropertiesContent = null;
		if (sslPropertiesFile.exists()) {
			sslPropertiesContent = FileUtil.readFile(sslPropertiesFile, monitor);
		}

		Set<TemplateProperty> result = new LinkedHashSet<TemplateProperty>(props.size());
		Enumeration e = props.propertyNames();
		while (e.hasMoreElements()) {
			String key = (String) e.nextElement();
			String defaultValue = null;
			if ("runtime.user".equals(key)) {
				// special case for a property from "base" template
				File initdShFile = new File(templateDir, "bin/init.d.sh");
				if (initdShFile.exists()) {
					String initdShContent = FileUtil.readFile(initdShFile, monitor);
					defaultValue = findDefaultValue(key, initdShContent);
				}
			}
			else {
				defaultValue = findDefaultValue(key, serverFragmentContent);
				if (defaultValue == null && sslPropertiesContent != null) {
					defaultValue = findDefaultValue(key, sslPropertiesContent);
				}
				if (defaultValue != null) {
					if (isLinked(defaultValue)) {
						defaultValue = resolveLink(result, defaultValue);
					}
					else if (!isResolved(defaultValue)) {
						defaultValue = resolve(defaultValue);
					}
				}
			}
			result.add(new TemplateProperty(templateDir.getName(), key, props.getProperty(key), defaultValue));
		}
		// TODO: cyclomatic complexity went through the roof
		// refactor when another "special case" is found
		return result;
	}

	private static boolean isLinked(String defaultValue) {
		return findLink(defaultValue) != null;
	}

	private static String findLink(String defaultValue) {
		Pattern pattern = Pattern.compile("\\$\\{([^:]+)\\}");
		Matcher matcher = pattern.matcher(defaultValue);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}

	private static String resolveLink(Set<TemplateProperty> propsFoundSoFar, String defaultValue) {
		String key = findLink(defaultValue);
		for (TemplateProperty prop : propsFoundSoFar) {
			if (prop.getKey().equals(key)) {
				return prop.getRawDefault();
			}
		}
		return defaultValue;
	}

	private static boolean isResolved(String defaultValue) {
		return findDefaultValue(".*", defaultValue) == null;
	}

	private static String resolve(String defaultValue) {
		String embeded = findDefaultValue(".*", defaultValue);
		return defaultValue.replaceFirst("\\$\\{.*:" + embeded + "\\}", embeded);
	}

	private static String findDefaultValue(String key, String serverFragmentContent) {
		Pattern pattern = Pattern.compile("\\$\\{" + key + ":([^=]+)\\}");
		Matcher matcher = pattern.matcher(serverFragmentContent);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
