/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.tests;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.PropertyWriterTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerBehaviourTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerConfiguratorImporterTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerDeploymentTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerInstanceTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerRuntimeClasspathProviderTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerRuntimeTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerTemplatePropertiesReaderTest;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerVersionHandlerTest;

/**
 * Runs all automated tests for tc Server support.
 *
 * @author Steffen Pingel
 * @author Tomasz Zarna
 */
@RunWith(Suite.class)
@SuiteClasses({ PropertyWriterTest.class, //
		TcServerBehaviourTest.class, //
		TcServerConfiguratorImporterTest.class, //
		TcServerDeploymentTest.class, //
		TcServerRuntimeClasspathProviderTest.class, //
		TcServerRuntimeTest.class, //
		TcServerVersionHandlerTest.class, //
		TcServerProvisioningTest.class, //
		TcServerTemplatePropertiesReaderTest.class, //
		TcServerInstanceTest.class //
})
public class AllTcServerTests {
	// goofy junit4, no class body needed
}
