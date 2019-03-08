/*******************************************************************************
 * Copyright (c) 2018 Pivotal Software, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 *******************************************************************************/
package com.vmware.vfabric.ide.eclipse.tcserver.internal.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.wst.server.core.IServer;
import org.junit.AfterClass;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;

import com.vmware.vfabric.ide.eclipse.tcserver.tests.support.TcServerFixture;

/**
 * tc Server 4.0 templates tests
 * 
 * @author Alex Boyko
 */
public class TcServer40TemplatePropertiesReaderTest {

	private static IServer server;

	private static TemplatePropertiesReader reader;

	@BeforeClass
	public static void beforeClass() throws Exception {
		server = TcServerFixture.V_4_0.createServer(null);
		reader = new TemplatePropertiesReader(server);
	}

	@Test
	public void haveTestsForAllTemplates() {
		Set<String> actualTemplates = Collections.emptySet();
		TcServerRuntime tcRuntime = (TcServerRuntime) server.getRuntime().loadAdapter(TcServerRuntime.class,
				new NullProgressMonitor());
		if (tcRuntime != null) {
			actualTemplates = tcRuntime.getTemplates();
		}
		String[] expecteds = new String[] { "ajp", "apr",
				"apr-ssl", "base", "cluster-node", "diagnostics", "jmx-ssl", "nio",
				"nio-ssl" };
		assertTrue("Not all basic templates are present in current tc server runtime installation",
				actualTemplates.containsAll(Arrays.asList(expecteds)));
	}

	@Test
	public void ajpTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("ajp", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props, "ajp", "http.port",
				"Please enter the port that the AJP connector should listen for requests on:", "8009");
		assertPropsEquals(props, "ajp", "https.port",
				"Please enter the port that the AJP connector should redirect SSL requests to:", "8443");
	}

	@Test
	public void aprTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("apr", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props, "apr", "http.port",
				"Please enter the port that the APR connector should listen for HTTP requests on:", "8080");
		assertPropsEquals(props, "apr", "https.port",
				"Please enter the port that the APR connector should redirect HTTPS requests to:", "8443");
	}

	@Test
	public void aprSslTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("apr-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(14, props.size());
		assertPropsEquals(props, "apr-ssl", "https.port",
				"Please enter the port that the APR connector should listen for HTTPS requests on:", "8443");
		assertPropsEquals(props, "apr-ssl", "ssl.protocol",
				"Please enter the SSL protocol(s) that the APR connector should use:", "all");
		assertPropsEquals(
				props,
				"apr-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use. To create a new name, leave blank:",
				"GENERATE");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "apr-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props, "apr-ssl", "ssl.certificate.location",
				"Please enter the path that the SSL certificate should be stored to:", "conf/tc-server.cer");
		assertPropsEquals(
				props,
				"apr-ssl",
				"ssl.certificate.location.input",
				"Please enter the path that the SSL certificate should be read from. To create a new certificate, leave blank:",
				"GENERATE");
		assertPropsEquals(props, "apr-ssl", "ssl.private.key.location",
				"Please enter the path that the SSL private key should be stored to:", "conf/tc-server.key");
		assertPropsEquals(props, "apr-ssl", "ssl.private.key.location.input",
				"Please enter the path that the SSL private key should be read from:");
	}

	@Test
	public void asyncLoggerTemplate() throws Exception {
		if (TcServerFixture.current().after(TcServerFixture.V_3_0)) {
			TcServerRuntime tcRuntime = (TcServerRuntime) server.getRuntime().loadAdapter(TcServerRuntime.class,
					new NullProgressMonitor());
			if (tcRuntime != null) {
				assertNull("async-logger template is available but is not expected",
						tcRuntime.getTemplateFolder("async-logger"));
			}
			else {
				fail("Tc Server runtime not found for current server!");
			}
		}
		else {
			Set<TemplateProperty> props = reader.read("async-logger", new NullProgressMonitor());
			Assume.assumeNotNull(props);
			assertEquals(0, props.size());
		}
	}

	@Test
	public void baseTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("base", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(3, props.size());
		assertPropsEquals(props, "base", "jmx.port",
				"Please enter the port that the JMX socket listener should listen on:", "6969");
		assertPropsEquals(props, "base", "shutdown.port",
				"Please enter the port that Tomcat Shutdown should listen on:", "-1");
		
		/*
		 * Tc Server 4.x installation doesn't have `bin/init.d.sh` file to fetch default for `runtime.user`
		 */
//		assertPropsEquals(props, "base", "runtime.user",
//				"Please enter the user account that should start the instance when using the 'bin/init.d.sh' script:",
//				"tcserver");
	}

	@Test
	public void bioTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("bio-tomcat-7", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props, "bio", "http.port",
				"Please enter the port that the BIO connector should listen for HTTP requests on:", "8080");
		assertPropsEquals(props, "bio", "https.port",
				"Please enter the port that the BIO connector should redirect HTTPS requests to:", "8443");
	}

	@Test
	public void bioSslTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("bio-ssl-tomcat-7", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(20, props.size());
		assertPropsEquals(props, "bio-ssl", "https.port",
				"Please enter the port that the BIO connector should listen for HTTPS requests on:", "8443");
		assertPropsEquals(props, "bio-ssl", "ssl.protocol",
				"Please enter the SSL protocol(s) that the BIO connector should use:", "TLS");
		assertPropsEquals(
				props,
				"bio-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use (i.e. cn=MyCompany,dc=mycompany,dc=com). To be prompted for name components leave blank:",
				"GENERATE");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "bio-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props, "bio-ssl", "ssl.key.alias",
				"Please enter the alias that the keystore should refer to the SSL private key as:", "tc-server-bio-ssl");
		assertPropsEquals(props, "bio-ssl", "ssl.key.alias.input",
				"Please enter the alias that the keystore refers to the SSL private key as:");
		assertPropsEquals(props, "bio-ssl", "ssl.key.password",
				"Please enter the password that keystore should protect the SSL private key with:", "RANDOM");
		assertPropsEquals(props, "bio-ssl", "ssl.key.password.input",
				"Please enter the password that keystore protects the SSL private key with:");
		assertPropsEquals(props, "bio-ssl", "ssl.keystore.location",
				"Please enter the path that the SSL keystore should be stored to:", "conf/tc-server-bio-ssl.keystore");
		assertPropsEquals(
				props,
				"bio-ssl",
				"ssl.keystore.location.input",
				"Please enter the path that the SSL keystore should be read from. To create a new keystore, leave blank:",
				"GENERATE");
		assertPropsEquals(props, "bio-ssl", "ssl.keystore.password",
				"Please enter the password that the SSL keystore should protect itself with:", "RANDOM");
		assertPropsEquals(props, "bio-ssl", "ssl.keystore.password.input",
				"Please enter the password that the SSL keystore protects itself with:");
	}

	@Test
	public void clusterNodeTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("cluster-node-tomcat-7", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(5, props.size());
		/*
		 * Tc Server 4.0 needs key `nods.name` to be corrected to `node-name` for tomcat-85 and tomcat-9
		 */
		assertPropsEquals(props, "cluster-node", "node.name",
				"Please enter the cluster node name used to identify this instance:", "tc-runtime-1");
		assertPropsEquals(props, "cluster-node", "membership.address",
				"Please enter the membership address for this instance:", "228.0.0.4");
		assertPropsEquals(props, "cluster-node", "membership.port",
				"Please enter the membership port for this instance:", "45564");
		assertPropsEquals(props, "cluster-node", "receiver.address",
				"Please enter the receiver address for this instance:", "auto");
		assertPropsEquals(props, "cluster-node", "receiver.port",
				"Please enter the receiver port for this instance:", "4000");
	}

	@Test
	public void diagnosticsTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("diagnostics", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(5, props.size());
		assertPropsEquals(props, "diagnostics", "jdbc.resource.name",
				"Please enter the JNDI name that the diagnostic DataSource should be available at:");
		assertPropsEquals(props, "diagnostics", "jdbc.username",
				"Please enter the username that the diagnostic DataSource should connect to the database with:");
		assertPropsEquals(props, "diagnostics", "jdbc.password",
				"Please enter the password that the diagnostic DataSource should connect to the database with:");
		assertPropsEquals(props, "diagnostics", "jdbc.driverClassName",
				"Please enter the JDBC driver class name that the diagnostic DataSource should connect to the database with:");
		assertPropsEquals(props, "diagnostics", "jdbc.url",
				"Please enter the JDBC URL that the diagnostic DataSource should connect to the database with:");
	}

	@Test
	public void jmxSslTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("jmx-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(18, props.size());
		assertPropsEquals(
				props,
				"jmx-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use. To be prompted for name components leave blank:",
				"GENERATE");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "jmx-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props, "jmx-ssl", "ssl.key.alias",
				"Please enter the alias that the keystore should refer to the SSL private key as:", "tc-server-jmx-ssl");
		assertPropsEquals(props, "jmx-ssl", "ssl.key.alias.input",
				"Please enter the alias that the keystore refers to the SSL private key as:");
		assertPropsEquals(props, "jmx-ssl", "ssl.key.password",
				"Please enter the password that keystore should protect the SSL private key with:", "RANDOM");
		assertPropsEquals(props, "jmx-ssl", "ssl.key.password.input",
				"Please enter the password that keystore protects the SSL private key with:");
		assertPropsEquals(props, "jmx-ssl", "ssl.keystore.location",
				"Please enter the path that the SSL keystore should be stored to:", "conf/tc-server-jmx-ssl.keystore");
		assertPropsEquals(
				props,
				"jmx-ssl",
				"ssl.keystore.location.input",
				"Please enter the path that the SSL keystore should be read from. To create a new keystore, leave blank:",
				"GENERATE");
		assertPropsEquals(props, "jmx-ssl", "ssl.keystore.password",
				"Please enter the password that the SSL keystore should protect itself with:", "RANDOM");
		assertPropsEquals(props, "jmx-ssl", "ssl.keystore.password.input",
				"Please enter the password that the SSL keystore protects itself with:");
	}

	@Test
	public void nioTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("nio", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(2, props.size());
		assertPropsEquals(props, "nio", "http.port",
				"Please enter the port that the NIO connector should listen for HTTP requests on:", "8080");
		assertPropsEquals(props, "nio", "https.port",
				"Please enter the port that the NIO connector should redirect HTTPS requests to:", "8443");
	}

	@Test
	public void nioSslTemplate() throws Exception {
		Set<TemplateProperty> props = reader.read("nio-ssl", new NullProgressMonitor());
		Assume.assumeNotNull(props);

		assertEquals(20, props.size());
		assertPropsEquals(props, "nio-ssl", "https.port",
				"Please enter the port that the NIO connector should listen for HTTPS requests on:", "8443");
		assertPropsEquals(props, "nio-ssl", "ssl.protocol",
				"Please enter the SSL protocol(s) that the NIO connector should use:", "TLS");
		assertPropsEquals(
				props,
				"nio-ssl",
				"ssl.certificate.dname",
				"Please enter the distinguised name the SSL certificate should use. To be prompted for name components leave blank:",
				"GENERATE");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.CN",
				"Please enter the first and last name the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.OU",
				"Please enter the organizational unit the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.O",
				"Please enter the organization the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.L",
				"Please enter the city or locality the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.ST",
				"Please enter the state or province the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.certificate.dname.C",
				"Please enter the two-letter country code the SSL certificate should use:", "Unknown");
		assertPropsEquals(props, "nio-ssl", "ssl.key.size",
				"Please enter the size in bits that the SSL private key should be:", "2048");
		assertPropsEquals(props, "nio-ssl", "ssl.key.alias",
				"Please enter the alias that the keystore should refer to the SSL private key as:", "tc-server-nio-ssl");
		assertPropsEquals(props, "nio-ssl", "ssl.key.alias.input",
				"Please enter the alias that the keystore refers to the SSL private key as:");
		assertPropsEquals(props, "nio-ssl", "ssl.key.password",
				"Please enter the password that keystore should protect the SSL private key with:", "RANDOM");
		assertPropsEquals(props, "nio-ssl", "ssl.key.password.input",
				"Please enter the password that keystore protects the SSL private key with:");
		assertPropsEquals(props, "nio-ssl", "ssl.keystore.location",
				"Please enter the path that the SSL keystore should be stored to:", "conf/tc-server-nio-ssl.keystore");
		assertPropsEquals(
				props,
				"nio-ssl",
				"ssl.keystore.location.input",
				"Please enter the path that the SSL keystore should be read from. To create a new keystore, leave blank:",
				"GENERATE");
		assertPropsEquals(props, "nio-ssl", "ssl.keystore.password",
				"Please enter the password that the SSL keystore should protect itself with:", "RANDOM");
		assertPropsEquals(props, "nio-ssl", "ssl.keystore.password.input",
				"Please enter the password that the SSL keystore protects itself with:");
	}

/*
 * `redis-session-manager` and 	`redis-session-manager-auth` don't come with installation by default and need to be installed separately
 */
	
//	@Test
//	public void redisSessionManager() throws Exception {
//		Set<TemplateProperty> props = reader.read("redis-session-manager", new NullProgressMonitor());
//		Assume.assumeNotNull(props);
//
//		assertEquals(5, props.size());
//		assertPropsEquals(props, "redis-session-manager", "poolTimeout",
//				"Please specify the timeout for the Redis connection pool:", "2000");
//		assertPropsEquals(props, "redis-session-manager", "connectionPoolSize",
//				"Please specify the size of the Redis connection pool:", "-1");
//		assertPropsEquals(props, "redis-session-manager", "databaseIndex", "Please specify the Redis database index:",
//				"0");
//		assertPropsEquals(props, "redis-session-manager", "host", "Please specify the Redis host name:", "localhost");
//		assertPropsEquals(props, "redis-session-manager", "port", "Please specify the Redis port number:", "6379");
//	}
//
//	@Test
//	public void redisSessionManagerAuth() throws Exception {
//		Set<TemplateProperty> props = reader.read("redis-session-manager-auth", new NullProgressMonitor());
//		Assume.assumeNotNull(props);
//
//		assertEquals(6, props.size());
//		assertPropsEquals(props, "redis-session-manager-auth", "poolTimeout",
//				"Please specify the timeout for the Redis connection pool:", "2000");
//		assertPropsEquals(props, "redis-session-manager-auth", "connectionPoolSize",
//				"Please specify the size of the Redis connection pool:", "-1");
//		assertPropsEquals(props, "redis-session-manager-auth", "databaseIndex",
//				"Please specify the Redis database index:", "0");
//		assertPropsEquals(props, "redis-session-manager-auth", "host", "Please specify the Redis host name:",
//				"localhost");
//		assertPropsEquals(props, "redis-session-manager-auth", "port", "Please specify the Redis port number:", "6379");
//		assertPropsEquals(props, "redis-session-manager-auth", "password",
//				"Please specify the password for the Redis authentication:");
//	}

	private void assertPropsEquals(Set<TemplateProperty> actual, String... expected) {
		for (TemplateProperty prop : actual) {
			if (prop.getKey().equals(expected[1])) {
				assertPropsEquals(prop, expected);
				return;
			}
		}
		fail("Could not find property with the given key: " + expected[1]);
	}

	private void assertPropsEquals(TemplateProperty actual, String... expected) {
		assertTrue("Template id doesn't match", actual.getTemplate().startsWith(expected[0]));
		assertEquals(expected[1], actual.getKey());
		assertEquals(expected[2], actual.getMessage());
		if (expected.length == 4) {
			assertEquals("Default values for " + expected[1] + " didn't match:", expected[3], actual.getRawDefault());
		}
		else {
			assertNull("Expecting null but found: " + actual.getRawDefault(), actual.getRawDefault());
		}
	}

	@AfterClass
	public static void afterClass() throws Exception {
		if (server != null) {
			TcServerFixture.deleteServerAndRuntime(server);
		}
	}
}
