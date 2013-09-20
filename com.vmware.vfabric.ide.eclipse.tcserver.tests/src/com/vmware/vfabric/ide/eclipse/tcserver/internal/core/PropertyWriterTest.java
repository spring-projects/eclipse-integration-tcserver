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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.PropertyWriter;

import junit.framework.TestCase;

/**
 * @author Steffen Pingel
 */
public class PropertyWriterTest extends TestCase {

	public void testEmptyProperties() throws IOException {
		Map<String, String> values = new HashMap<String, String>();
		PropertyWriter writer = new PropertyWriter(values);
		assertEquals("a\nb\n", writer.apply("a\nb\n"));
	}

	public void testEmptyString() throws IOException {
		Map<String, String> values = new HashMap<String, String>();
		values.put("key", "value");
		PropertyWriter writer = new PropertyWriter(values);
		assertEquals("key=value\n", writer.apply(""));
	}

	public void testEncoding() throws IOException {
		Map<String, String> values = new HashMap<String, String>();
		values.put("key", ":value");
		PropertyWriter writer = new PropertyWriter(values);
		assertEquals("key=\\:value\n", writer.apply(""));
	}

	public void testNoTrailingNewLine() throws IOException {
		Map<String, String> values = new HashMap<String, String>();
		PropertyWriter writer = new PropertyWriter(values);
		assertEquals("", writer.apply(""));
		assertEquals(" \n", writer.apply(" "));
		assertEquals("a\n", writer.apply("a"));
		assertEquals("ab\nc\n", writer.apply("ab\nc"));
	}

	public void testPreserveComment() throws IOException {
		Map<String, String> values = new HashMap<String, String>();
		PropertyWriter writer = new PropertyWriter(values);
		assertEquals("# \n", writer.apply("# "));
	}

	public void testReplace() throws IOException {
		Map<String, String> values = new HashMap<String, String>();
		values.put("key", "new");
		PropertyWriter writer = new PropertyWriter(values);
		assertEquals("key=new\n", writer.apply("key=value\n"));
		assertEquals("#comment\n\n\nvalue\nkey=new\nkey2=value\n", writer
				.apply("#comment\n\n\nvalue\nkey=value\nkey2=value\n"));
		values.put("newkey", "new");
		assertEquals("#comment\n\n\nvalue\nkey=new\nkey2=value\nnewkey=new\n", writer
				.apply("#comment\n\n\nvalue\nkey=value\nkey2=value\n"));
	}

	public void testTrailingNewLine() throws IOException {
		Map<String, String> values = new HashMap<String, String>();
		PropertyWriter writer = new PropertyWriter(values);
		assertEquals("\n", writer.apply("\n"));
	}

}
