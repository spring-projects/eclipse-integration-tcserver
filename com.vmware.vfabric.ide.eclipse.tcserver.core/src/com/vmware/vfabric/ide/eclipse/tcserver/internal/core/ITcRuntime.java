/*******************************************************************************
 * Copyright (c) 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import org.eclipse.core.runtime.IPath;

/**
 * tc server runtime common functionality
 * (See id template container and template folder must be declared here as well)
 * 
 * @author Alex Boyko
 *
 */
public interface ITcRuntime {
	
	IPath runtimeLocation();
	
	IPath instanceCreationScript();
	
	IPath getTomcatLocation();
	
	IPath getTomcatServersContainer();
	
	IPath instanceDirectory(String instanceName);
	
	IPath defaultInstancesDirectory();

}
