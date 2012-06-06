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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.eclipse.osgi.util.NLS;

/**
 * Writes properties to a string using the Java properties format while
 * preserving comments and blank lines.
 * @author Steffen Pingel
 */
public class PropertyWriter {

	private final Properties p = new Properties();

	private final Map<String, String> values;

	public PropertyWriter(Map<String, String> values) {
		this.values = values;
	}

	public String apply(String content) throws IOException {
		// make a copy to allow multiple calls to apply()
		Map<String, String> changedValues = new HashMap<String, String>(values);

		StringBuilder sb = new StringBuilder();
		BufferedReader reader = new BufferedReader(new StringReader(content));

		// read property file line by line to preserve commments and ordering
		String line;
		while ((line = reader.readLine()) != null) {
			if (line.length() > 0 && !line.startsWith("#")) {
				// parse line
				p.clear();
				p.load(new ByteArrayInputStream(line.getBytes()));
				Enumeration<Object> enumeration = p.keys();
				if (enumeration.hasMoreElements()) {
					// check if property has changed
					String key = (String) enumeration.nextElement();
					String value = changedValues.get(key);
					if (value != null) {
						// replace with new value
						line = entry(key, value);
						changedValues.remove(key);
					}
				}
			}
			sb.append(line);
			sb.append("\n");
		}

		// append all properties that haven't changed
		for (Entry<String, String> entry : changedValues.entrySet()) {
			sb.append(entry(entry.getKey(), entry.getValue()));
			sb.append("\n");
		}

		return sb.toString();
	}

	private String entry(String key, String value) throws IOException {
		p.clear();
		p.put(key, value);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		p.store(out, null);
		BufferedReader reader = new BufferedReader(new StringReader(out.toString()));
		String line;
		while ((line = reader.readLine()) != null) {
			// ignore comments
			if (!line.startsWith("#")) {
				return line;
			}
		}
		throw new IOException(NLS.bind("Unexpected error reading property from ''{0}''", out.toString()));
	}

}
