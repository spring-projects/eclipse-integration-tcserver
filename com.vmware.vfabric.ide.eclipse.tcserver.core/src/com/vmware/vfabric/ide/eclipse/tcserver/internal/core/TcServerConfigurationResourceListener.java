package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jst.server.tomcat.core.internal.ConfigurationResourceListener;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerType;
import org.eclipse.wst.server.core.ServerCore;
import org.eclipse.wst.server.core.internal.ServerType;

// Alas, ConfigurationResourceListener has a value hard-coded in that makes it Tomcat-specific
public class TcServerConfigurationResourceListener extends ConfigurationResourceListener {

	private IProject serversProject;

	private final String VMWARE_SERVER_ID_PREFIX = "com.vmware.server.tc.";

	private final String SPRINGSOURCE_SERVER_ID_PREFIX = "com.springsource.tcserver.";

	public void resourceChanged(IResourceChangeEvent event) {
		if (event.getType() == IResourceChangeEvent.POST_CHANGE) {
			IProject project = getServersProject();
			if (project != null) {
				IResourceDelta delta = event.getDelta();
				if (delta != null) {
					IResourceDelta serversProjectDelta = delta.findMember(project.getFullPath());
					if (serversProjectDelta != null) {
						// The change occurred within the Servers project.
						IResourceDelta[] childDelta = serversProjectDelta.getAffectedChildren();
						if (childDelta.length > 0) {
							IServer[] servers = ServerCore.getServers();
							for (IResourceDelta element : childDelta) {
								// Check if this subfolder of the Servers folder
								// matches a Tomcat configuration folder
								for (IServer server : servers) {
									IServerType serverType = server.getServerType();
									if (serverType.getId().startsWith(VMWARE_SERVER_ID_PREFIX)
											|| serverType.getId().startsWith(SPRINGSOURCE_SERVER_ID_PREFIX)) {
										IFolder configFolder = server.getServerConfiguration();
										if (configFolder != null) {
											if (element.getFullPath().equals(configFolder.getFullPath())) {
												// Found a Tomcat server
												// affected by this delta.
												// Update this server's publish
												// state.
												TcServerBehaviour tcServerBehaviour = (TcServerBehaviour) server
														.loadAdapter(TcServerBehaviour.class, null);
												if (tcServerBehaviour != null) {
													// Indicate that this server
													// needs to publish and
													// restart if running
													tcServerBehaviour
															.setTomcatServerPublishState(IServer.PUBLISH_STATE_INCREMENTAL);
													tcServerBehaviour.setTomcatServerRestartState(true);
												}
												break;
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
	}

	private IProject getServersProject() {
		if (serversProject == null) {
			IProject project;
			try {
				project = ServerType.getServerProject();
				synchronized (this) {
					serversProject = project;
				}
			}
			catch (CoreException e) {
				// Ignore
			}
		}
		return serversProject;
	}

}
