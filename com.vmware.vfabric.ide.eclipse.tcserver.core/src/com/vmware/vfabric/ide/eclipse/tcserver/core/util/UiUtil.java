/*******************************************************************************
 * Copyright (c) 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.core.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.internal.browser.WebBrowserPreference;
import org.eclipse.ui.internal.browser.WorkbenchBrowserSupport;

@SuppressWarnings("restriction")
public class UiUtil {
	
	public static void openUrl(final String location) {
		// Must be running in the UI thread to open a browser and *surprisingly*
		// also to call getBrowserChoice. Otherwise a invalid thread access
		// exception may
		// ensue on windows and mac os and then internal browser is permanently
		// disabled.
		// This only happens sometimes.
		// maybe it depends on if the preferences have been initialized before.
		// In any case we need the asyncExec here!!!
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				uiThreadOpenUrl(location, WebBrowserPreference.getBrowserChoice());
			}
		});
	}
	
	/**
	 * Don't call this method unless you are in the UI thread!
	 */
	private static void uiThreadOpenUrl(String location, int browserChoice) {
		try {
			URL url = null;
			if (location != null) {
				url = new URL(location);
			}
			if (browserChoice == WebBrowserPreference.EXTERNAL) {
				try {
					IWorkbenchBrowserSupport support = PlatformUI.getWorkbench().getBrowserSupport();
					support.getExternalBrowser().openURL(url);
				}
				catch (Exception e) {
				}
			}
			else {
				IWebBrowser browser;
				int flags;
				if (WorkbenchBrowserSupport.getInstance().isInternalWebBrowserAvailable()) {
					flags = IWorkbenchBrowserSupport.AS_EDITOR | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}
				else {
					flags = IWorkbenchBrowserSupport.AS_EXTERNAL | IWorkbenchBrowserSupport.LOCATION_BAR
							| IWorkbenchBrowserSupport.NAVIGATION_BAR;
				}

				String generatedId = "org.eclipse.mylyn.web.browser-" + Calendar.getInstance().getTimeInMillis();
				browser = WorkbenchBrowserSupport.getInstance().createBrowser(flags, generatedId, null, null);
				browser.openURL(url);
			}
		}
		catch (PartInitException e) {
			MessageDialog.openError(Display.getDefault().getActiveShell(), "Failed to Open Browser",
					"Browser could not be initiated");
		}
		catch (MalformedURLException e) {
			if (location == null || location.trim().equals("")) {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Failed to Open Browser",
						"No URL to open." + location);
			}
			else {
				MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Failed to Open Browser",
						"Could not open URL: " + location);
			}
		}
	}
	
	

}
