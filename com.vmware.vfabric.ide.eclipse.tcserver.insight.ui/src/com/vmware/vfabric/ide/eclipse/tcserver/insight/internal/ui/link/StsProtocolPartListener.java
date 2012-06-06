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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.eclipse.swt.browser.Browser;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.browser.BrowserViewer;
import org.eclipse.ui.internal.browser.WebBrowserEditor;
import org.springsource.ide.eclipse.commons.core.SpringCoreUtils;

import com.vmware.vfabric.ide.eclipse.tcserver.insight.internal.ui.Activator;

/**
 * {@link IPartListener} that registers the STS embedded cookie and the
 * {@link StsProtocolLocationListener}.
 * @author Christian Dupuis
 * @author Steffen Pingel
 * @since 2.2.0
 */
@SuppressWarnings("restriction")
public class StsProtocolPartListener implements IPartListener {

	private final StsProtocolLocationListener locationListener = new StsProtocolLocationListener();

	/**
	 * {@inheritDoc}
	 */
	public void partActivated(IWorkbenchPart part) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void partBroughtToTop(IWorkbenchPart part) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void partClosed(IWorkbenchPart part) {
		if (part instanceof WebBrowserEditor) {

			// Remove Link Handler
			Browser browser = getBrowser((WebBrowserEditor) part);
			if (browser != null) {
				browser.removeLocationListener(locationListener);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void partDeactivated(IWorkbenchPart part) {
	}

	/**
	 * {@inheritDoc}
	 */
	public void partOpened(IWorkbenchPart part) {
		if (part instanceof WebBrowserEditor) {

			if (SpringCoreUtils.isEclipseSameOrNewer(3, 5)) {
				// Add Cookies first; this is Eclipse 3.5 API and therefore we
				// can't do it on < 3.5
				installCookies();
			}

			// Add Link Handler
			Browser browser = getBrowser((WebBrowserEditor) part);
			if (browser != null) {
				browser.addLocationListener(locationListener);
			}
		}
	}

	// TODO e3.5 replace by Browser.setCookie();
	private void setCookie(String value, String url) {
		try {
			Method method = Browser.class.getMethod("setCookie", String.class, String.class);
			method.invoke(null, value, url);
		}
		catch (Exception e) {
			// ignore
		}
	}

	private void installCookies() {
		setCookie(Activator.COOKIE, "http://localhost" + Activator.CONTEXT);
		setCookie(Activator.COOKIE, "https://localhost" + Activator.CONTEXT);
		setCookie(Activator.COOKIE, "http://127.0.0.1" + Activator.CONTEXT);
		setCookie(Activator.COOKIE, "https://127.0.0.1" + Activator.CONTEXT);

		try {
			InetAddress adr = InetAddress.getLocalHost();
			setCookie(Activator.COOKIE, "http://" + adr.getHostName() + Activator.CONTEXT);
			setCookie(Activator.COOKIE, "https://" + adr.getHostName() + Activator.CONTEXT);
			setCookie(Activator.COOKIE, "http://" + adr.getHostAddress() + Activator.CONTEXT);
			setCookie(Activator.COOKIE, "https://" + adr.getHostAddress() + Activator.CONTEXT);
		}
		catch (UnknownHostException e) {
			Activator.log(e);
		}
	}

	private Browser getBrowser(final WebBrowserEditor browserEditor) {
		try { // HACK: using reflection to gain accessibility
			Class<?> browserClass = browserEditor.getClass();
			Field browserField = browserClass.getDeclaredField("webBrowser");
			browserField.setAccessible(true);
			Object browserObject = browserField.get(browserEditor);
			if (browserObject != null && browserObject instanceof BrowserViewer) {
				return ((BrowserViewer) browserObject).getBrowser();
			}
		}
		catch (Exception e) {
			Activator.log(e);
		}
		return null;
	}

}
