/*******************************************************************************
 * Copyright (c) 2012 - 2013 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jst.server.tomcat.ui.internal.editor.ServerLocationEditorSection;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * @author Steffen Pingel
 * @uathor Leo Dos Santos
 */
public class TcServerLocationEditorSection extends ServerLocationEditorSection {

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);

		updateLabel(serverDirMetadata);
		updateLabel(serverDirInstall);
		updateLabel(serverDirCustom);
	}

	@Override
	public IStatus[] getSaveStatus() {
		if (tomcatServer != null) {
			String dir = tomcatServer.getInstanceDirectory();
			if (dir != null) {
				IPath path = new Path(dir);
				if (path.equals(installDirPath)) {
					return new IStatus[] { Status.OK_STATUS };
				}
			}
		}
		return super.getSaveStatus();
	}

	@Override
	protected void initialize() {
		super.initialize();

		if (serverDir == null || tomcatServer == null) {
			return;
		}

		// the runtime points to the tc Server base directory and not catalina
		// home by default but the location editor expects catalina home
		TcServer tcServer = (TcServer) server.loadAdapter(TcServer.class, null);
		if (tcServer != null) {
			installDirPath = tcServer.getInstanceBase(tcServer.getServer().getRuntime());
		}

		updating = true;
		updateServerDirButtons();
		updateServerDirFields();
		updating = false;
		validate();
	}

	private void updateLabel(Button button) {
		String label = button.getText();
		label = label.replaceAll("Tomcat", "tc Server");
		button.setText(label);
	}

	@Override
	protected void updateServerDirButtons() {
		super.updateServerDirButtons();
		if (tomcatServer.getInstanceDirectory() != null) {
			IPath path = tomcatServer.getRuntimeBaseDirectory();
			if (path != null && path.equals(installDirPath)) {
				serverDirInstall.setSelection(true);
				serverDirMetadata.setSelection(false);
				serverDirCustom.setSelection(false);
			}
		}
	}

	@Override
	protected void validate() {
		super.validate();
		if (tomcatServer != null) {
			String dir = tomcatServer.getInstanceDirectory();
			if (dir != null) {
				IPath path = new Path(dir);
				if (path.equals(installDirPath)) {
					setErrorMessage(null);
				}
			}
		}
	}

}
