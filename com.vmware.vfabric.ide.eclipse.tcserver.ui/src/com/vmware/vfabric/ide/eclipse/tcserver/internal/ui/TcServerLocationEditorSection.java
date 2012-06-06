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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import org.eclipse.jst.server.tomcat.ui.internal.editor.ServerLocationEditorSection;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * @author Steffen Pingel
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
		updating = false;
		validate();
	}

	private void updateLabel(Button button) {
		String label = button.getText();
		label = label.replaceAll("Tomcat", "tc Server");
		button.setText(label);
	}

}
