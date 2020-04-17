package com.vmware.vfabric.ide.eclipse.tcserver.tests.support;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.wst.server.core.IServerWorkingCopy;

/**
 * @author Steffen Pingel
 */
public abstract class ServerHandlerCallback {

	public abstract void configureServer(IServerWorkingCopy wc) throws CoreException;

}
