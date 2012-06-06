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
package com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.link;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.browser.Browser;
import org.eclipse.swt.browser.LocationEvent;
import org.eclipse.swt.browser.LocationListener;
import org.eclipse.swt.widgets.Display;

import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.Activator;
import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.ILocationHandler;

/**
 * {@link LocationListener} implementation that is resonsible for the <code>sts://</code> protocol.
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.2.0
 */
public class StsProtocolLocationListener implements LocationListener {

	private static final String STS_PROTOCOL = "sts://";

	private static final Pattern PATTERN = Pattern.compile("sts://(.*)\\((.*)\\)");
	
	private Map<String, ILocationHandler> handlers = new HashMap<String, ILocationHandler>();

	public StsProtocolLocationListener() {
		handlers.put("openJavaElement", new JavaElementLocationHandler());
	}

	public void changed(LocationEvent event) {
		// nothing to do
	}

	public void changing(LocationEvent event) {
		String location = event.location;
		if (location.startsWith(STS_PROTOCOL)) {
			event.doit = false;
			
			location = decodeString(location);

			Matcher matcher = PATTERN.matcher(location);
			if (matcher.matches()) {
				String handlerName = matcher.group(1);
				String parameter = matcher.group(2);

				if (handlers.containsKey(handlerName)) {
					handlers.get(handlerName).handleLocation(getServerUrl(event), parameter);
				}
				else {
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Action not registered",
							"The selected action '" + handlerName + "' is not known in this version of STS");
				}
			}
		}
	}

	private String decodeString(String location) {
		try {
			location = URLDecoder.decode(location, "UTF-8");
		}
		catch (UnsupportedEncodingException e) {
			Activator.log(e);
		}
		return location;
	}
	
	public String getServerUrl(LocationEvent event) {
		String browserUrl = ((Browser) event.getSource()).getUrl();
		try {
			URL url = new URL(browserUrl);
			return url.getProtocol() + "://" + url.getHost() + ":" + url.getPort();
		}
		catch (MalformedURLException e) {
			Activator.log(e);
		}
		return null;
	}
	

}
