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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.JmxServicabilityInfo;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerConfiguration;

/**
 * Server configuration editor section for configuring tc Server serviceability.
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class ServicabilityEditorSection extends ServerEditorSection {

	protected TcServerConfiguration configuration;

	private JmxServicabilityInfo info;

	private Label statusLabel;

	private TcServer serverInstance;

	private PropertyChangeListener listener;

	public ServicabilityEditorSection() {
	}

	protected void addChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (TcServerConfiguration.MODIFY_SERVER_PROPERTY_PROPERTY.equals(event.getPropertyName())) {
					update();
				}
			}
		};
		configuration.addPropertyChangeListener(listener);
	}

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR
				| Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		section.setText("Servicability");
		section.setDescription("Specify settings for servicability.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 8;
		layout.marginWidth = 8;
		composite.setLayout(layout);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		statusLabel = toolkit.createLabel(composite, "");
		GridDataFactory.fillDefaults().span(2, 1).applyTo(statusLabel);

		initialize();
	}

	@Override
	public void dispose() {
		if (configuration != null) {
			configuration.removePropertyChangeListener(listener);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		serverInstance = (TcServer) server.loadAdapter(TcServer.class, null);
		try {
			configuration = serverInstance.getTomcatConfiguration();
		}
		catch (Exception e) {
			// ignore
		}
		addChangeListener();
		initialize();
	}

	/**
	 * Initialize the fields in this editor.
	 */
	protected void initialize() {
		update();
	}

	private void update() {
		if (statusLabel == null || statusLabel.isDisposed()) {
			return;
		}

		String label;
		info = configuration.getServicabilityInfo(serverInstance.getRuntimeBaseDirectory());
		if (info != null) {
			label = NLS.bind("JMX connection configured at {0}.", info.getConnectionLabel());
		}
		else {
			label = "JMX is not configured.";
		}
		if (!label.equals(statusLabel.getText())) {
			statusLabel.setText(label);
			statusLabel.getParent().layout(new Control[] { statusLabel });
		}
	}
}
