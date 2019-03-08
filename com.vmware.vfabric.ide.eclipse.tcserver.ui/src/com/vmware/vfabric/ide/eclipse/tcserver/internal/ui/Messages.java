/*******************************************************************************
 * Copyright (c) 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

/**
 * String messages to show in the UI
 *
 * @author Alex Boyko
 *
 */
public class Messages {

	public static final String UNKNOWN_INSTANCE_TOMCAT_VERSION = "Cannot determine Tomcat version of the tc Server Instance. Instance folder may not be a child of Server Runtime folder";

	public static final String TOMCAT_VERSION_MISMATCH = "Errors may occur due to mismatched Tomcat versions. Runtime Tomcat version is {0}. Instance Tomcat version is {1}.";

}
