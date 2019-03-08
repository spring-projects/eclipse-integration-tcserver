/*******************************************************************************
 * Copyright (c) 2012 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * A section for editing the enablement for the Spring Insight application
 * running on tc Server.
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class TcServerInsightSection extends ServerEditorSection {

	private TcServer serverInstance;

	private PropertyChangeListener listener;

	private Button button;

	public TcServerInsightSection() {
	}

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.TITLE_BAR
				| Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE | ExpandableComposite.EXPANDED);
		section.setText("Overview");
		section.setDescription("Enable collection of performance metrics for deployed applications.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 8;
		layout.marginWidth = 8;
		composite.setLayout(layout);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		button = toolkit.createButton(composite, "Enable gathering of metrics", SWT.CHECK);
		button.addSelectionListener(new SelectionAdapter() {
			private boolean ignoreEvents;

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (ignoreEvents) {
					return;
				}
				if (button.getSelection() && server.getOriginal() != null
						&& !TcServerInsightUtil.isInsightCompatible(server.getOriginal())) {
					MessageDialog dialog = new MessageDialog(
							getShell(),
							"Enable Spring Insight",
							null,
							"This version of Spring Insight is not compatible with tc Server instances that are installed on a path that contains spaces. Enabling Spring Insight may cause tc Server to fail to startup.\n\n"
									+ "Are you sure you want to enable Spring Insight?", MessageDialog.ERROR,
							new String[] { IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL }, 1);
					if (dialog.open() != 0) {
						try {
							ignoreEvents = true;
							button.setSelection(false);
						}
						finally {
							ignoreEvents = false;
						}
						return;
					}
				}
				execute(new ModifyInsightVmArgsCommand(serverInstance, button.getSelection()));
			}
		});
		GridDataFactory.fillDefaults().span(2, 1).applyTo(button);

		initialize();
	}

	@Override
	public void dispose() {
		if (server != null) {
			server.removePropertyChangeListener(listener);
		}
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		serverInstance = (TcServer) server.loadAdapter(TcServer.class, null);
		addChangeListener();
		initialize();
	}

	private void update() {
		if (button != null && !button.isDisposed()) {
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					button.setSelection(!serverInstance.getAddExtraVmArgs().containsAll(
							Arrays.asList(TcServerInsightUtil.DISABLED_INSIGHT)));
				}
			});
		}
	}

	protected void addChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (TcServer.PROPERTY_ADD_EXTRA_VMARGS.equals(event.getPropertyName())) {
					update();
				}
			}
		};
		server.addPropertyChangeListener(listener);
	}

	/**
	 * Initialize the fields in this editor.
	 */
	protected void initialize() {
		update();
	}
}
