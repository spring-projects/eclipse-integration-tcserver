/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.junit.After;
import org.junit.Ignore;
import org.junit.Test;
import org.springsource.ide.eclipse.commons.configurator.ServerHandler;
import org.springsource.ide.eclipse.commons.core.FileUtil;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerTestPlugin;

/**
 * @author Steffen Pingel
 * @author Tomasz Zarna
 */
public class TcServerVersionHandlerTest {

	private ServerHandler handler;

	private IServer server;

	protected File baseDir;

	protected File tomcatDir;

	protected File tomcatConfDir;

	@After
	public void tearDown() throws Exception {
		if (handler != null) {
			handler.deleteServerAndRuntime(new NullProgressMonitor());
		}
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}

	@Test
	@Ignore("Ignoring tcServer-6.0 tests")
	public void testVerifyInstallPath60TwoResourceTags() throws Exception {
		handler = TcServerFixture.V_6_0.provisionServer();
		baseDir = new File(handler.getServerPath());
		tomcatDir = new File(baseDir, "tomcat-6.0.19.A");
		tomcatConfDir = new File(tomcatDir, "conf");

		File source = StsTestUtil.getFilePath(TcServerTestPlugin.PLUGIN_ID, "/testdata/resources-test/server.xml");
		FileUtil.copyFile(source, new File(tomcatConfDir, "server.xml"), new NullProgressMonitor());

		server = handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
		server.publish(IServer.PUBLISH_FULL, null);
		String originalServerXml = FileUtil.readFile(source, new NullProgressMonitor());
		File targetFile = new File(tomcatConfDir, "server.xml");
		String publishedServerXml = FileUtil.readFile(targetFile, new NullProgressMonitor());
		assertEquals(StsTestUtil.canocalizeXml(originalServerXml), StsTestUtil.canocalizeXml(publishedServerXml));
	}

	@Test
	@Ignore("Ignoring tcServer-6.0 tests")
	public void testVerifyInstallPath60() throws Exception {
		handler = TcServerFixture.V_6_0.provisionServer();
		baseDir = new File(handler.getServerPath());
		tomcatDir = new File(baseDir, "tomcat-6.0.19.A");
		tomcatConfDir = new File(tomcatDir, "conf");

		server = handler.createServer(new NullProgressMonitor(), ServerHandler.ALWAYS_OVERWRITE);
		server.publish(IServer.PUBLISH_FULL, null);
		File source = StsTestUtil.getFilePath(TcServerTestPlugin.PLUGIN_ID,
				"/testdata/tcServer-6.0/tomcat-6.0.19.A/conf/server.xml");
		String originalServerXml = FileUtil.readFile(source, new NullProgressMonitor());
		String publishedServerXml = FileUtil.readFile(new File(tomcatConfDir, "server.xml"), new NullProgressMonitor());
		assertEquals(StsTestUtil.canocalizeXml(originalServerXml), StsTestUtil.canocalizeXml(publishedServerXml));
	}

}
