/*******************************************************************************
 * Copyright (c) 2012, 2015 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.MessageFormat;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.internal.ServerWorkingCopy;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerUtil;

/**
 * Server configuration editor section for configuring tc Server specific
 * information such as the selected instance.
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class TcServerInfoEditorSection extends ServerEditorSection {

	private PropertyChangeListener runtimeChangelistener;

	private static final String PROPERTY_RUNTIME = "runtime-id"; //$NON-NLS-1$

	private Label serverNameLabel;

	private TcServer serverInstance;

	private Label infoLabel;

	public TcServerInfoEditorSection() {
	}

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR
				| Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		section.setText("Server Instance");
		section.setDescription("Specify settings for tc Server.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 8;
		layout.marginWidth = 8;
		composite.setLayout(layout);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		infoLabel = toolkit.createLabel(composite, "");
		infoLabel.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));

		serverNameLabel = toolkit.createLabel(composite, "");

		initialize();
	}

	protected void addChangeListeners() {
		runtimeChangelistener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (PROPERTY_RUNTIME.equals(event.getPropertyName())) {
					validate();
				}
			}
		};
		server.addPropertyChangeListener(runtimeChangelistener);
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		serverInstance = (TcServer) server.loadAdapter(TcServer.class, null);
		initialize();
	}

	/**
	 * Initialize the fields in this editor.
	 */
	protected void initialize() {
		if (serverNameLabel == null) {
			return;
		}

		if (serverInstance.isAsfLayout()) {
			infoLabel.setText("The server is configured for ASF layout.");
			serverNameLabel.setText("");
		}
		else {
			String serverName = serverInstance.getServerName();
			infoLabel.setText("Instance:");
			serverNameLabel.setText(serverName + " (" + serverInstance.getLayout().toString() + ")");
		}
		addChangeListeners();
	}

	@Override
	public void dispose() {
		super.dispose();
		if (server != null) {
			server.removePropertyChangeListener(runtimeChangelistener);
		}
	}

	protected void validate() {
		setErrorMessage(null);
		String tomcatRuntimeVersion = TcServerUtil.getServerVersion(server.getRuntime());
		String instanceTomcatVersion = TcServerUtil.getInstanceTomcatVersion(TcServerUtil
				.getInstanceDirectory((ServerWorkingCopy) server));
		if (instanceTomcatVersion == null) {
			setErrorMessage(Messages.UNKNOWN_INSTANCE_TOMCAT_VERSION);
		}
		else if (!tomcatRuntimeVersion.equals(instanceTomcatVersion)) {
			setErrorMessage(MessageFormat.format(Messages.TOMCAT_VERSION_MISMATCH, tomcatRuntimeVersion,
					instanceTomcatVersion));
		}
	}

}
