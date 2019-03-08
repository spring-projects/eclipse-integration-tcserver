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
package com.vmware.vfabric.ide.eclipse.tcserver.insight.tests;

import junit.framework.Test;
import junit.framework.TestSuite;

import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.InsightPluginModelTest;
import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.InsightPluginTest;
import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.InsightTcServerCallbackTest;

/**
 * @author Steffen Pingel
 */
public class AllInsightTests {

	public static Test suite() {
		TestSuite suite = new TestSuite(AllInsightTests.class.getName());
		suite.addTestSuite(InsightPluginTest.class);
		suite.addTestSuite(InsightPluginModelTest.class);
		suite.addTestSuite(InsightTcServerCallbackTest.class);
		return suite;
	}

}
