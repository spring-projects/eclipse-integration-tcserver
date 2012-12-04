/*******************************************************************************
 * Copyright (c) 2012 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

/**
 * @author Tomasz Zarna
 *
 */
public class TemplateProperty {

	private static final String GENERATE = "GENERATE";

	private static final String RANDOM = "RANDOM";

	private final String template;

	private final String key;

	private final String message;

	private final String defaultValue;

	private String value;

	public TemplateProperty(String template, String key, String message, String defaultValue) {
		this.template = template;
		this.key = key;
		this.message = removeDefaultPlaceholderIfExists(message);
		this.defaultValue = defaultValue;
		this.value = defaultValue;
	}

	private String removeDefaultPlaceholderIfExists(String message) {
		if (message.endsWith(". Default '${default}':")) {
			return message.replace(". Default '${default}':", ":");
		}
		return message;
	}

	public String getTemplate() {
		return this.template;
	}

	public String getKey() {
		return this.key;
	}

	public String getMessage() {
		return this.message;
	}

	/**
	 * @return the default value for the given property, can be
	 * <code>null</code>
	 */
	public String getRawDefault() {
		return defaultValue;
	}

	public String getDefault() {
		if (defaultValue == null || GENERATE.equals(defaultValue) || RANDOM.equals(defaultValue)) {
			return "";
		}
		else {
			return defaultValue;
		}
	}

	public String getValue() {
		return value;
	}

	public void setValue(String newValue) {
		this.value = newValue;
	}

	public boolean isDefault() {
		if (value == null) {
			return false;
		}
		return value.equals(defaultValue);
	}
}
