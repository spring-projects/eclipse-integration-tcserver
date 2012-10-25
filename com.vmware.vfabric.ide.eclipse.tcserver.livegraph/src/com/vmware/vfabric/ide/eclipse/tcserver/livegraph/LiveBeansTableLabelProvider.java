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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jst.server.tomcat.ui.internal.TomcatUIPlugin;
import org.eclipse.swt.graphics.Image;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;
import org.eclipse.wst.server.ui.ServerUICore;

/**
 * @author Leo Dos Santos
 */
@SuppressWarnings("restriction")
public class LiveBeansTableLabelProvider extends LabelProvider {

	private final IServer server;

	private final ILabelProvider delegate;

	public LiveBeansTableLabelProvider(IServer server) {
		super();
		this.server = server;
		delegate = ServerUICore.getLabelProvider();
	}

	@Override
	public void dispose() {
		delegate.dispose();
		super.dispose();
	}

	@Override
	public Image getImage(Object element) {
		if (element instanceof String) {
			IModule[] modules = server.getModules();
			for (IModule module : modules) {
				if (element.equals(module.getName())) {
					return delegate.getImage(module);
				}
			}
			return TomcatUIPlugin.getImage(TomcatUIPlugin.IMG_WEB_MODULE);
		}
		return super.getImage(element);
	}

}
