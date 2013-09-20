/*******************************************************************************
 * Copyright (c) 2012 GoPivotal, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     GoPivotal, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.ui;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.resource.ImageDescriptor;

/**
 * Provides image descriptors for tc Server.
 * @author Steffen Pingel
 * @author Christian Dupuis
 */
public class TcServerImages {

	private static final URL baseURL = TcServerUiPlugin.getDefault().getBundle().getEntry("/icons/"); //$NON-NLS-1$

	private final static String WIZBAN = "wizban/"; // basic colors - size 16x16

	public static final ImageDescriptor WIZB_SERVER = create(WIZBAN, "springsource_wiz.png");

	public static ImageDescriptor create(String prefix, String name) {
		try {
			return ImageDescriptor.createFromURL(makeIconFileURL(prefix, name));
		}
		catch (MalformedURLException e) {
			return ImageDescriptor.getMissingImageDescriptor();
		}
	}

	private static URL makeIconFileURL(String prefix, String name) throws MalformedURLException {
		if (baseURL == null) {
			throw new MalformedURLException();
		}

		StringBuffer buffer = new StringBuffer();
		buffer.append("full");
		buffer.append('/');
		buffer.append(prefix);
		buffer.append('/');
		buffer.append(name);
		return new URL(baseURL, buffer.toString());
	}

}
