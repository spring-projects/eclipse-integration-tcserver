/*******************************************************************************
 * Copyright (c) 2012, 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.vmware.vfabric.ide.eclipse.tcserver.configurator.ConfigurableExtension;
import com.vmware.vfabric.ide.eclipse.tcserver.configurator.ServerHandler;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.configurator.ConfiguratorImporter;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.StsTestUtil;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * @author Steffen Pingel
 * @author Tomasz Zarna
 */
@RunWith(Parameterized.class)
public class TcServerConfiguratorImporterTest {

	@Rule
	public TemporaryFolder location = new TemporaryFolder();

	private final TcServerFixture[] fixtures;

	public TcServerConfiguratorImporterTest(TcServerFixture[] fixtures) {
		this.fixtures = fixtures;
	}

	@Parameters
	public static Collection<Object[]> data() {
		Object[][] data = new Object[][] {
				{ new TcServerFixture[] { TcServerFixture.V_2_7, TcServerFixture.V_2_8, TcServerFixture.V_2_9 } },
				{ new TcServerFixture[] { TcServerFixture.V_3_0, TcServerFixture.V_4_0 } } };
		return Arrays.asList(data);
	}

	@Test
	public void testDetect() throws Exception {
		ConfiguratorImporter importer = new ConfiguratorImporter();
		importer.setScanInstallPath(false);
		provisionAndCopyFixtures(location.getRoot(), fixtures);
		importer.setSearchLocations(Collections.singletonList(location.getRoot()));

		List<ConfigurableExtension> extensions = importer.detectExtensions(new NullProgressMonitor());

		for (TcServerFixture fixture : fixtures) {
			assertContains(fixture.getDescription(), extensions);
		}
	}

	private void assertContains(String id, List<ConfigurableExtension> extensions) {
		for (ConfigurableExtension extension : extensions) {
			if (extension.getId().startsWith(id)) {
				assertTrue("Expected auto configuration flag for extension " + extension,
						extension.isAutoConfigurable());
				return;
			}
		}
		fail("Expected extension with id prefix '" + id + "' in " + StringUtils.join(extensions, ", "));
	}

	private void provisionAndCopyFixtures(final File destination, TcServerFixture... fixtures) throws Exception {
		for (TcServerFixture fixture : fixtures) {
			ServerHandler serverHandler = fixture.provisionServer();
			File target = new File(destination, fixture.getDescription());
			StsTestUtil.copyDirectory(new File(serverHandler.getServerPath()), target);
		}
	}
}
