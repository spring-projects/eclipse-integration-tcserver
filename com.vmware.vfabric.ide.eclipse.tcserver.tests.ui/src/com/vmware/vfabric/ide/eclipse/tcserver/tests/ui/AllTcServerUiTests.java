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
package com.vmware.vfabric.ide.eclipse.tcserver.tests.ui;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.springframework.ide.eclipse.uaa.IUaa;
import org.springframework.ide.eclipse.uaa.UaaPlugin;

/**
 * @author Kaitlin Duck Sherwood
 */
public class AllTcServerUiTests {

	static {
		System.out.println("Setting UAA privacy level to 'LIMITED_DATA'");
		UaaPlugin.getUAA().setPrivacyLevel(IUaa.LIMITED_DATA);
	}

	public static Test suite() {
		TestSuite suite = new TestSuite(AllTcServerUiTests.class.getName());
		suite.addTestSuite(TcServerNewServerWizardUiTest.class);
		return suite;
	}

}
