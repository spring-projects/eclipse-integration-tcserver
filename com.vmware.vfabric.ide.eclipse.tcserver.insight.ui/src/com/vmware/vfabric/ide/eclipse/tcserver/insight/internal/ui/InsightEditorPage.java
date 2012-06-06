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
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.forms.ManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.wst.server.ui.editor.ServerEditorPart;
import org.eclipse.wst.server.ui.internal.ImageResource;

/**
 * @author Steffen Pingel
 */
public class InsightEditorPage extends ServerEditorPart implements IEditorPart {

	private ManagedForm managedForm;

	private final List<IInsightPageParticipant> participants;

	public InsightEditorPage() {
		participants = new ArrayList<IInsightPageParticipant>();
	}

	public void addPageParticipant(IInsightPageParticipant participant) {
		participants.add(participant);
	}

	@Override
	public void createPartControl(Composite parent) {
		managedForm = new ManagedForm(parent);
		setManagedForm(managedForm);
		ScrolledForm form = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());
		form.setText("Spring Insight");
		form.setImage(ImageResource.getImage(ImageResource.IMG_SERVER));
		form.getBody().setLayout(new GridLayout());

		Composite columnComp = toolkit.createComposite(form.getBody());
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 10;
		columnComp.setLayout(layout);
		columnComp.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		// left column
		Composite leftColumnComp = toolkit.createComposite(columnComp);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.verticalSpacing = 10;
		layout.horizontalSpacing = 0;
		leftColumnComp.setLayout(layout);
		leftColumnComp.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

		insertSections(leftColumnComp, "com.springsource.sts.server.insight.ui.left");

		// right column
		// Composite rightColumnComp = toolkit.createComposite(columnComp);
		// layout = new GridLayout();
		// layout.marginHeight = 0;
		// layout.marginWidth = 0;
		// layout.verticalSpacing = 10;
		// layout.horizontalSpacing = 0;
		// rightColumnComp.setLayout(layout);
		// rightColumnComp.setLayoutData(new GridData(GridData.FILL,
		// GridData.FILL, true, true));
		//
		// insertSections(rightColumnComp,
		// "com.springsource.sts.server.insight.ui.right");
	}

	@Override
	public void doSave(IProgressMonitor monitor) {
		super.doSave(monitor);
		for (IInsightPageParticipant participant : participants) {
			participant.doSave(monitor);
		}
	}

	public void removePageParticipant(IInsightPageParticipant participant) {
		participants.remove(participant);
	}

	@Override
	public void setFocus() {
		// ignore
	}

}
