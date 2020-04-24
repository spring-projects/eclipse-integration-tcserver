/*******************************************************************************
 * Copyright (c) 2012, 2020 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.configurator.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.equinox.internal.p2.repository.Transport;
import org.eclipse.equinox.p2.core.IProvisioningAgent;
import org.eclipse.osgi.util.NLS;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.vmware.vfabric.ide.eclipse.tcserver.internal.configurator.Activator;

/**
 * Provides helper methods for downloading files.
 * @author Steffen Pingel
 */
public class HttpUtil {

	public static IStatus download(String url, File archiveFile, File targetDirectory, IProgressMonitor monitor) {
		return download(url, archiveFile, targetDirectory, null, monitor);
	}

	public static IStatus download(String url, File archiveFile, File targetDirectory, String prefix,
			IProgressMonitor monitor) {

		if (monitor.isCanceled()) {
			return Status.CANCEL_STATUS;
		}

		SubMonitor progress = SubMonitor.convert(monitor, 100);

		targetDirectory.mkdirs();

		// download archive file
		try {
			try {
				OutputStream out = new BufferedOutputStream(new FileOutputStream(archiveFile));
				try {
					HttpUtil.download(new URI(url), out, progress.newChild(70));
				}
				catch (CoreException e) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID,
							NLS.bind("I/O error while retrieving data: {0}", e.getMessage()), e);
				}
				catch (URISyntaxException e) {
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, NLS.bind("Invalid URL: {0}", url), e);
				}
				finally {
					out.close();
				}
			}
			catch (IOException e) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "I/O error while retrieving data", e);
			}

			// extract archive file
			try {
				URL fileUrl = archiveFile.toURI().toURL();
				ZipFileUtil.unzip(fileUrl, targetDirectory, prefix, progress.newChild(30));
				if (targetDirectory.listFiles().length <= 0) {
					String message = NLS.bind("Zip file {0} appears to be empty", archiveFile);
					return new Status(IStatus.ERROR, Activator.PLUGIN_ID, message);
				}
			}
			catch (IOException e) {
				return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Error while extracting archive", e);
			}
		}
		finally {
			archiveFile.delete();
		}
		return Status.OK_STATUS;
	}

	public static void download(URI uri, OutputStream out, IProgressMonitor monitor) throws CoreException {
		String protocol = uri.getScheme();
		if ("file".equals(protocol)) {
			// Yes. it is a bit strange that HttpUtil knows how to read from
			// file url. But it is just easier that
			// way. Don't need to special case file urls in other places.
			// We should consider renaming this class but it has the potential
			// of breaking a lot of dependencies.
			File f = new File(uri);
			FileInputStream contents = null;
			try {
				contents = new FileInputStream(f);
				byte[] buf = new byte[40 * 1024];
				int read;
				while ((read = contents.read(buf)) >= 0) {
					// read = -1 means EOF
					// read == 0 probably is impossible but handle it anyway.
					if (read > 0) {
						out.write(buf, 0, read);
					}
				}
			}
			catch (IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, e.getMessage(), e));
			}
			finally {
				try {
					if (contents != null) {
						contents.close();
					}
				}
				catch (IOException e) {
				}
			}
		}
		else {
			downloadFromWeb(uri, out, monitor);
		}
	}

	private static void downloadFromWeb(URI uri, OutputStream out, IProgressMonitor monitor) throws CoreException {
		IStatus result = getTransport().download(uri, out, monitor);
		if (result.getSeverity() == IStatus.CANCEL) {
			throw new OperationCanceledException();
		}
		if (!result.isOK()) {
			throw new CoreException(result);
		}
	}

	private static Transport transport;

	private static Transport getTransport() {
		if (transport == null) {
			BundleContext bundleContext = Activator.getDefault().getBundle().getBundleContext();
			ServiceReference<IProvisioningAgent> serviceReference = bundleContext
					.getServiceReference(IProvisioningAgent.class);
			IProvisioningAgent provisioningAgent = bundleContext.getService(serviceReference);
			transport = provisioningAgent.getService(Transport.class);
			bundleContext.ungetService(serviceReference);
		}
		return transport;
	}

	public static void ping(URI uri) throws MalformedURLException, IOException, CoreException {
		URLConnection connection = uri.toURL().openConnection();
		connection.setConnectTimeout(500);
		InputStream input = connection.getInputStream();
		consume(input);
	}

	private static final int BUF_SIZE = 1024;

	/**
	 * Consume all data in an input stream, discdaring the data and closing the
	 * stream upon completion.
	 */
	private static void consume(InputStream stream) throws IOException {
		try {
			byte[] buf = new byte[BUF_SIZE];
			while (stream.read(buf) >= 0) {
			}
		}
		finally {
			try {
				stream.close();
			}
			catch (IOException e) {
				// ignore (or it may mask a more important / interesting error
				// in the body of the try
			}
		}
	}

}
