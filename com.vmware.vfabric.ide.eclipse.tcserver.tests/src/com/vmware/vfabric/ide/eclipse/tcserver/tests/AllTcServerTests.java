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
package com.vmware.vfabric.ide.eclipse.tcserver.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.PropertyWriterTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerBehaviourTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerConfiguratorImporterTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerDeploymentTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerRuntimeClasspathProviderTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerRuntimeTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerVersionHandlerTest;
import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * @author Steffen Pingel
 */
public class AllTcServerTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTcServerTests.class.getName());
		suite.addTestSuite(PropertyWriterTest.class);
		suite.addTestSuite(TcServerBehaviourTest.class);
		suite.addTestSuite(TcServerConfiguratorImporterTest.class);
		suite.addTestSuite(TcServerDeploymentTest.class);
		suite.addTestSuite(TcServerRuntimeClasspathProviderTest.class);
		suite.addTestSuite(TcServerRuntimeTest.class);
		suite.addTestSuite(TcServerVersionHandlerTest.class);

		for (TcServerFixture configuration : TcServerFixture.ALL) {
			configuration.createSuite(suite);
			if (configuration.getDownloadUrl() != null) {
				configuration.add(TcServerProvisioningTest.class);
			}
			configuration.done();
		}
		return suite;
	}

}
