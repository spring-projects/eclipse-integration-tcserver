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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.IFormColors;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * Server configuration editor section for configuring tc Server Deployer MBean
 * properties.
 * @author Steffen Pingel
 */
public class DeployerEditorSection extends ServerEditorSection {

	private class PropertyModifyListener implements ModifyListener {

		private final String property;

		public PropertyModifyListener(String property) {
			this.property = property;
		}

		public void modifyText(ModifyEvent e) {
			if (updating) {
				return;
			}
			try {
				updating = true;
				execute(new ModifyDeployerPropertyCommand(serverInstance, property, ((Text) e.widget).getText()));
			}
			finally {
				updating = false;
			}

		}
	}

	private TcServer serverInstance;

	private PropertyChangeListener listener;

	private Text userText;

	protected boolean updating;

	private Text passwordText;

	private Text portText;

	private Text serviceText;

	private Text hostText;

	public DeployerEditorSection() {
	}

	protected void addChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				update(event.getPropertyName());
			}
		};
		serverInstance.getServerWorkingCopy().addPropertyChangeListener(listener);
	}

	protected Label createLabel(FormToolkit toolkit, Composite parent, String text) {
		Label label = toolkit.createLabel(parent, text);
		label.setForeground(toolkit.getColors().getColor(IFormColors.TITLE));
		return label;
	}

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR
				| Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE | ExpandableComposite.EXPANDED);
		section.setText("Deployment");
		section.setDescription("Specify settings for deployment.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 8;
		layout.marginWidth = 8;
		composite.setLayout(layout);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		createLabel(toolkit, composite, "Port:");
		portText = toolkit.createText(composite, "");
		portText.addModifyListener(new PropertyModifyListener(TcServer.PROPERTY_JMX_PORT));
		portText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createLabel(toolkit, composite, "Username:");
		userText = toolkit.createText(composite, "");
		userText.addModifyListener(new PropertyModifyListener(TcServer.PROPERTY_JMX_USER));
		userText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createLabel(toolkit, composite, "Password:");
		passwordText = toolkit.createText(composite, "", SWT.PASSWORD);
		passwordText.addModifyListener(new PropertyModifyListener(TcServer.PROPERTY_JMX_PASSWORD));
		passwordText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// spacer
		GridDataFactory.fillDefaults().span(2, 1).applyTo(createLabel(toolkit, composite, ""));

		createLabel(toolkit, composite, "Service:");
		serviceText = toolkit.createText(composite, "");
		serviceText.addModifyListener(new PropertyModifyListener(TcServer.PROPERTY_DEPLOYER_SERVICE));
		serviceText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		createLabel(toolkit, composite, "Host:");
		hostText = toolkit.createText(composite, "");
		hostText.addModifyListener(new PropertyModifyListener(TcServer.PROPERTY_DEPLOYER_HOST));
		hostText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		initialize();
	}

	@Override
	public void dispose() {
		if (serverInstance != null) {
			serverInstance.getServerWorkingCopy().removePropertyChangeListener(listener);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		serverInstance = (TcServer) server.loadAdapter(TcServer.class, null);
		addChangeListener();
		initialize();
	}

	/**
	 * Initialize the fields in this editor.
	 */
	protected void initialize() {
		update(null);
	}

	private void update(String property) {
		if (updating || userText == null || userText.isDisposed()) {
			return;
		}
		try {
			updating = true;

			updateText(TcServer.PROPERTY_JMX_USER, userText, property);
			updateText(TcServer.PROPERTY_JMX_PASSWORD, passwordText, property);
			updateText(TcServer.PROPERTY_JMX_PORT, portText, property);

			updateText(TcServer.PROPERTY_DEPLOYER_SERVICE, serviceText, property);
			updateText(TcServer.PROPERTY_DEPLOYER_HOST, hostText, property);
		}
		finally {
			updating = false;
		}
	}

	private void updateText(String property, Text text, String changedProperty) {
		if (property.equals(changedProperty) || changedProperty == null) {
			String value = serverInstance.getDeployerProperty(property);
			text.setText((value != null) ? value : "");
		}
	}

}
