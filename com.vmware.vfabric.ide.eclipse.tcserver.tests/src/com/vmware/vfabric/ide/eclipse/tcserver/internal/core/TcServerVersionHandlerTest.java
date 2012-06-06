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

import java.io.File;

import junit.framework.TestCase;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.springsource.ide.eclipse.commons.configurator.ServerHandler;
import org.springsource.ide.eclipse.commons.core.FileUtil;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerTestPlugin;

/**
 * @author Steffen Pingel
 */
public class TcServerVersionHandlerTest extends TestCase {

	private ServerHandler handler;

	private IServer server;

	protected File baseDir;

	protected File tomcatDir;

	protected File tomcatConfDir;

	@Override
	protected void tearDown() throws Exception {
		if (handler != null) {
			handler.deleteServerAndRuntime(new NullProgressMonitor());
		}
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}

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
