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
import java.util.Collections;
import java.util.List;

import junit.framework.TestCase;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.springsource.ide.eclipse.commons.configurator.ConfigurableExtension;
import org.springsource.ide.eclipse.commons.internal.configurator.ConfiguratorImporter;
import org.springsource.ide.eclipse.commons.tests.util.StsTestUtil;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerTestPlugin;

/**
 * @author Steffen Pingel
 */
public class TcServerConfiguratorImporterTest extends TestCase {

	public void testDetectTcServer20and21() throws Exception {
		ConfiguratorImporter importer = new ConfiguratorImporter();
		importer.setScanInstallPath(false);
		File location = StsTestUtil.getFilePath(TcServerTestPlugin.PLUGIN_ID, "/testdata");
		importer.setSearchLocations(Collections.singletonList(location));
		List<ConfigurableExtension> extensions = importer.detectExtensions(new NullProgressMonitor());
		assertContains(TcServerFixture.V_2_0.getStubLocation().getName(), extensions);
		assertContains(TcServerFixture.V_2_1.getStubLocation().getName(), extensions);
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

}
