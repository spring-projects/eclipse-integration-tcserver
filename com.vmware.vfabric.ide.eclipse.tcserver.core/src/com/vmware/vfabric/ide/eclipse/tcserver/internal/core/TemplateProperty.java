/*******************************************************************************
 * Copyright (c) 2012, 2014 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		Pattern pattern = Pattern.compile("(\\.[^\\.]*\\$\\{default\\}[^\\.]*(\\.|:))");
		Matcher matcher = pattern.matcher(message);
		if (matcher.find()) {
			return message.replace(matcher.group(1), ":");
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + ((template == null) ? 0 : template.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		TemplateProperty other = (TemplateProperty) obj;
		if (key == null) {
			if (other.key != null) {
				return false;
			}
		}
		else if (!key.equals(other.key)) {
			return false;
		}
		if (template == null) {
			if (other.template != null) {
				return false;
			}
		}
		else if (!template.equals(other.template)) {
			return false;
		}
		return true;
	}
}
