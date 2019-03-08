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
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jst.server.tomcat.ui.internal.ContextIds;
import org.eclipse.jst.server.tomcat.ui.internal.TomcatUIPlugin;
import org.eclipse.jst.server.tomcat.ui.internal.editor.ConfigurationPortEditorSection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.help.IWorkbenchHelpSystem;
import org.eclipse.wst.server.ui.editor.ServerEditorSection;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.ServerProperty;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServer;
import com.vmware.vfabric.ide.eclipse.tcserver.internal.core.TcServerConfiguration;

/**
 * A section for editing the server ports that supports place holders for ports.
 * Based on {@link ConfigurationPortEditorSection}.
 * @author Steffen Pingel
 */
public class PropertiesSection extends ServerEditorSection {

	/**
	 * The keys that are displayed in the table. Other properties are filtered.
	 */
	private final static Set<String> KEYS = new HashSet<String>(Arrays.asList(new String[] { //
			"tcserver.node", }));

	protected TcServerConfiguration configuration;

	protected boolean updating;

	protected Table propertiesTable;

	protected TableViewer viewer;

	protected PropertyChangeListener listener;

	/**
	 * ConfigurationPortEditorSection constructor comment.
	 */
	public PropertiesSection() {
		super();
	}

	/**
	 * 
	 */
	protected void addChangeListener() {
		listener = new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				if (TcServerConfiguration.MODIFY_SERVER_PROPERTY_PROPERTY.equals(event.getPropertyName())) {
					String key = (String) event.getOldValue();
					String value = (String) event.getNewValue();
					changeProperty(key, value);
				}
			}
		};
		configuration.addPropertyChangeListener(listener);
	}

	protected void changeProperty(String key, String value) {
		TableItem[] items = propertiesTable.getItems();
		int size = items.length;
		for (int i = 0; i < size; i++) {
			ServerProperty sp = (ServerProperty) items[i].getData();
			if (sp.getKey().equals(key)) {
				items[i].setData(new ServerProperty(key, value));
				items[i].setText(1, value);
				return;
			}
		}
	}

	@Override
	public void createSection(Composite parent) {
		super.createSection(parent);
		FormToolkit toolkit = getFormToolkit(parent.getDisplay());

		Section section = toolkit.createSection(parent, ExpandableComposite.TWISTIE | ExpandableComposite.EXPANDED
				| ExpandableComposite.TITLE_BAR | Section.DESCRIPTION | ExpandableComposite.FOCUS_TITLE);
		section.setText("Properties");
		section.setDescription("Modify the server properties.");
		section.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL));

		// properties
		Composite composite = toolkit.createComposite(section);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 8;
		layout.marginWidth = 8;
		composite.setLayout(layout);
		composite.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.FILL_HORIZONTAL));
		IWorkbenchHelpSystem whs = PlatformUI.getWorkbench().getHelpSystem();
		whs.setHelp(composite, ContextIds.CONFIGURATION_EDITOR_PORTS);
		toolkit.paintBordersFor(composite);
		section.setClient(composite);

		propertiesTable = toolkit.createTable(composite, SWT.V_SCROLL | SWT.H_SCROLL | SWT.FULL_SELECTION);
		propertiesTable.setHeaderVisible(true);
		propertiesTable.setLinesVisible(true);
		whs.setHelp(propertiesTable, ContextIds.CONFIGURATION_EDITOR_PORTS_LIST);

		TableLayout tableLayout = new TableLayout();

		TableColumn col = new TableColumn(propertiesTable, SWT.NONE);
		col.setText("Key");
		ColumnWeightData colData = new ColumnWeightData(15, 150, true);
		tableLayout.addColumnData(colData);

		col = new TableColumn(propertiesTable, SWT.NONE);
		col.setText("Value");
		colData = new ColumnWeightData(8, 60, true);
		tableLayout.addColumnData(colData);

		GridData data = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
		data.widthHint = 230;
		data.heightHint = 100;
		propertiesTable.setLayoutData(data);
		propertiesTable.setLayout(tableLayout);

		viewer = new TableViewer(propertiesTable);
		viewer.setColumnProperties(new String[] { "name", "port" });

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

		TcServer ts = (TcServer) server.loadAdapter(TcServer.class, null);
		try {
			configuration = ts.getTomcatConfiguration();
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
		if (propertiesTable == null) {
			return;
		}

		propertiesTable.removeAll();

		List<ServerProperty> properties = configuration.getProperties();
		// sort by key
		Collections.sort(properties, new Comparator<ServerProperty>() {
			public int compare(ServerProperty o1, ServerProperty o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		Iterator<ServerProperty> iterator = properties.iterator();
		while (iterator.hasNext()) {
			ServerProperty property = iterator.next();
			if (KEYS.contains(property.getKey()) || property.getKey().endsWith(".port")) {
				TableItem item = new TableItem(propertiesTable, SWT.NONE);
				String[] s = new String[] { property.getKey(), property.getValue() };
				item.setText(s);
				item.setImage(TomcatUIPlugin.getImage(TomcatUIPlugin.IMG_PORT));
				item.setData(property);
			}
		}

		if (readOnly) {
			viewer.setCellEditors(new CellEditor[] { null, null });
			viewer.setCellModifier(null);
		}
		else {
			setupPortEditors();
		}
	}

	protected void setupPortEditors() {
		viewer.setCellEditors(new CellEditor[] { null, new TextCellEditor(propertiesTable) });

		ICellModifier cellModifier = new ICellModifier() {
			public boolean canModify(Object element, String property) {
				if ("port".equals(property)) {
					return true;
				}

				return false;
			}

			public Object getValue(Object element, String property) {
				ServerProperty sp = (ServerProperty) element;
				return sp.getValue();
			}

			public void modify(Object element, String property, Object value) {
				try {
					Item item = (Item) element;
					ServerProperty sp = (ServerProperty) item.getData();
					execute(new ModifyServerPropertyCommand(configuration, sp.getKey(), (String) value));
				}
				catch (Exception ex) {
					// ignore
				}
			}
		};
		viewer.setCellModifier(cellModifier);

		// preselect second column (Windows-only)
		String os = System.getProperty("os.name");
		if (os != null && os.toLowerCase().indexOf("win") >= 0) {
			propertiesTable.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent event) {
					try {
						int n = propertiesTable.getSelectionIndex();
						viewer.editElement(propertiesTable.getItem(n).getData(), 1);
					}
					catch (Exception e) {
						// ignore
					}
				}
			});
		}
	}
}
