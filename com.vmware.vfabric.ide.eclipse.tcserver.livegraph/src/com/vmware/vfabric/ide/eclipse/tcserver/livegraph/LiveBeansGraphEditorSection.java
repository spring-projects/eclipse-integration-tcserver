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
package com.vmware.vfabric.ide.eclipse.tcserver.livegraph;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.management.remote.JMXConnector;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.core.IServerListener;
import org.eclipse.wst.server.core.ServerEvent;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModel;
import org.springframework.ide.eclipse.beans.ui.livegraph.model.LiveBeansModelGenerator;
import org.springframework.ide.eclipse.beans.ui.livegraph.views.LiveBeansGraphView;
import org.springsource.ide.eclipse.commons.core.StatusHandler;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;

/**
 * @author Leo Dos Santos
 */
public class LiveBeansGraphEditorSection extends ServerEditorSection {

	private TcServer serverWorkingCopy;

	private PropertyChangeListener propertyListener;

	private IServerListener stateListener;

	private Button enableMbeanButton;

	private List<Hyperlink> hyperlinks;

	private void addPropertyChangeListener() {
		propertyListener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (TcServer.PROPERTY_ADD_EXTRA_VMARGS.equals(evt.getPropertyName())) {
					update();
				}
			}
		};
		server.addPropertyChangeListener(propertyListener);
	}

	private void addServerStateListener() {
		stateListener = new IServerListener() {
			public void serverChanged(ServerEvent event) {
				if ((event.getKind() & ServerEvent.STATE_CHANGE) != 0) {
					if (event.getState() == IServer.STATE_STARTED) {
						setHyperlinkState(true);
					}
					else if (event.getState() == IServer.STATE_STOPPED) {
						setHyperlinkState(false);
					}
				}
			}
		};
		server.getOriginal().addServerListener(stateListener);
	}

	private void connectToApplication(IModule module) {
		JMXConnector connector = null;
		try {
			connector = serverWorkingCopy.getJmxConnector();
			LiveBeansModel model = LiveBeansModelGenerator.connectToModel(connector, module.getName());
			IViewPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.showView(LiveBeansGraphView.VIEW_ID);
			if (part instanceof LiveBeansGraphView) {
				((LiveBeansGraphView) part).setInput(model);
			}
		}
		catch (IOException e) {
			StatusHandler.log(new Status(IStatus.ERROR, TcServerLiveGraphPlugin.PLUGIN_ID,
					"An error occurred while connecting to server.", e));
		}
		catch (PartInitException e) {
			StatusHandler.log(new Status(IStatus.ERROR, TcServerLiveGraphPlugin.PLUGIN_ID,
					"An error occurred while opening the Live Beans Graph View.", e));
		}
		finally {
			if (connector != null) {
				try {
					connector.close();
				}
				catch (IOException e) {
					StatusHandler.log(new Status(IStatus.ERROR, TcServerLiveGraphPlugin.PLUGIN_ID,
							"An error occurred while closing connection to server.", e));
				}
			}
		}
	}

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
				| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		section.setText("Live Beans Graph");
		section.setDescription("Enable and launch the Live Beans Graph for deployed applications.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 5;
		layout.marginWidth = 1;
		layout.verticalSpacing = 5;
		layout.horizontalSpacing = 10;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		enableMbeanButton = toolkit.createButton(composite,
				"Enable Live Beans indexing (takes effect upon server restart)", SWT.CHECK);
		enableMbeanButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				execute(new ModifyLiveGraphVmArgsCommand(serverWorkingCopy, enableMbeanButton.getSelection()));
			}
		});
		GridDataFactory.fillDefaults().grab(true, false).applyTo(enableMbeanButton);

		toolkit.createLabel(composite, "Click to view application (requires Spring 3.2):");
		Label separator = toolkit.createSeparator(composite, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(separator);

		IServer original = server.getOriginal();
		hyperlinks = new ArrayList<Hyperlink>();
		if (original != null) {
			IModule[] modules = original.getModules();
			for (final IModule module : modules) {
				final Hyperlink link = toolkit.createHyperlink(composite, module.getName(), SWT.NONE);
				hyperlinks.add(link);
				GridDataFactory.fillDefaults().indent(5, 0).applyTo(link);
				link.addHyperlinkListener(new HyperlinkAdapter() {
					@Override
					public void linkActivated(HyperlinkEvent e) {
						connectToApplication(module);
					}
				});
			}
		}

		initialize();
	}

	@Override
	public void dispose() {
		if (server != null) {
			server.removePropertyChangeListener(propertyListener);
			server.getOriginal().removeServerListener(stateListener);
		}
		super.dispose();
	}

	@Override
	public void init(IEditorSite site, IEditorInput input) {
		super.init(site, input);
		serverWorkingCopy = (TcServer) server.loadAdapter(TcServer.class, null);
		addPropertyChangeListener();
		addServerStateListener();
		initialize();
	}

	private void initialize() {
		setHyperlinkState(server.getOriginal().getServerState() == IServer.STATE_STARTED);
		update();
	}

	private void setHyperlinkState(final boolean enabled) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				if (hyperlinks != null) {
					for (Hyperlink link : hyperlinks) {
						if (!link.isDisposed()) {
							link.setEnabled(enabled);
						}
					}
				}
			}
		});
	}

	private void update() {
		if (enableMbeanButton != null && !enableMbeanButton.isDisposed()) {
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					enableMbeanButton.setSelection(serverWorkingCopy.getAddExtraVmArgs().containsAll(
							Arrays.asList(TcServerLiveGraphPlugin.FLAG_LIVE_BEANS)));
				}
			});
		}
	}

}
