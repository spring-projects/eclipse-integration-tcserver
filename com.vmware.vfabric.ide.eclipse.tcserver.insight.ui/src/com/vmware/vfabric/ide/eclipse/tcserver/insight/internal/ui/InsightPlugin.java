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
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * @author Steffen Pingel
 */
public class InsightPlugin implements Comparable<InsightPlugin> {

	private class XmlHandler extends DefaultHandler {

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (INSIGHT_NAMESPACE.equals(uri)) {
				if ("plugin".equals(localName)) {
					setName(attributes.getValue("name"));
					setVersion(attributes.getValue("version"));
					setPublisher(attributes.getValue("publisher"));
				}
			}

		}

	}

	private static String INSIGHT_NAMESPACE = "http://www.springframework.org/schema/insight-idk";

	public static final String PREFIX = "insight-plugin-";

	public static final String EXT_DISABLED = ".jar.disabled";

	public static final String EXT_ENABLED = ".jar";

	private boolean enabled;

	private File file;

	private String name;

	private String publisher;

	private String version;

	public InsightPlugin(File file) {
		this.file = file;
		updateNameFromFile();
		setEnabled(isEnabled(file));
		parse(file);
	}

	public boolean commit() {
		if (isEnabled() != isEnabled(file)) {
			if (isEnabled()) {
				return rename(EXT_DISABLED, EXT_ENABLED);
			}
			else {
				return rename(EXT_ENABLED, EXT_DISABLED);
			}
		}
		return true;
	}

	public int compareTo(InsightPlugin p) {
		return getName().compareTo(p.getName());
	}

	public String getDetails() {
		StringBuffer sb = new StringBuffer();
		if (version != null) {
			sb.append(version);
		}
		if (publisher != null) {
			sb.append(" by ");
			sb.append(publisher);
		}
		return sb.toString();
	}

	public File getFile() {
		return file;
	}

	public String getName() {
		return name;
	}

	public String getPublisher() {
		return publisher;
	}

	public String getVersion() {
		return version;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public void setFile(File file) {
		this.file = file;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	private boolean isEnabled(File file) {
		return file.getName().endsWith(EXT_ENABLED);
	}

	private void parse(File file) {
		ZipFile zipFile;
		try {
			zipFile = new ZipFile(file);
			try {
				Enumeration<? extends ZipEntry> enumeration = zipFile.entries();
				while (enumeration.hasMoreElements()) {
					ZipEntry entry = enumeration.nextElement();
					if (entry.getName().startsWith("META-INF/insight-plugin-") && entry.getName().endsWith(".xml")) {
						InputStream in = zipFile.getInputStream(entry);
						try {
							XMLReader parser = XMLReaderFactory.createXMLReader();
							XmlHandler handler = new XmlHandler();
							parser.setContentHandler(handler);
							parser.parse(new InputSource(in));
						}
						catch (SAXException e) {
							// ignore
						}
						finally {
							in.close();
						}
					}
				}
			}
			finally {
				zipFile.close();
			}
		}
		catch (IOException e) {
			// ignore
		}
	}

	private boolean rename(String oldExt, String newExt) {
		String fileName = file.getName();
		if (fileName.endsWith(oldExt)) {
			String newName = fileName.substring(0, fileName.length() - oldExt.length()) + newExt;
			File targetFile = new File(file.getParentFile(), newName);
			boolean result = file.renameTo(targetFile);
			if (result) {
				setFile(targetFile);
			}
			return result;
		}
		return false;
	}

	private void updateNameFromFile() {
		String name = file.getName();
		if (name.startsWith(PREFIX)) {
			name = name.substring(PREFIX.length());
		}
		if (name.endsWith(EXT_DISABLED)) {
			name = name.substring(0, name.length() - EXT_DISABLED.length());
		}
		else if (name.endsWith(EXT_ENABLED)) {
			name = name.substring(0, name.length() - EXT_ENABLED.length());
		}
		this.name = name;
	}

}
