package controllers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Hashtable;
import java.util.Map;

import javax.management.MBeanServerConnection;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import play.Logger;

public class JMXConnectionManagement {

	private JMXConnector jmxcon = null;
	private MBeanServerConnection mbsc = null;
	
	
	public JMXConnector getJmxcon() {
		return this.jmxcon;
	}

	
	public void setJmxcon(JMXConnector jmxcon) {
		this.jmxcon = jmxcon;
	}

	
	public MBeanServerConnection getMbeanServerConection() {
		return this.mbsc;
	}

	
	public void setMbsc(MBeanServerConnection mbsc) {
		this.mbsc = mbsc;
	}

	
	public void close() {
		this.mbsc=null;
		try {
			jmxcon.close();
		} catch (IOException ignore) {}
	}
	
	
	private JMXConnectionManagement() {
		
	}
	
	
	public synchronized static JMXConnectionManagement createJMXConnection() throws Exception {
		JMXServiceURL jmxurl = null;
		JMXConnectionManagement jmxconmgmt = new JMXConnectionManagement();

		jmxurl = getJMXUrl();

		Map<String, String[]> env = getJMXConnectionProperties();

		try {
			jmxconmgmt.setJmxcon(JMXConnectorFactory.connect(jmxurl,env));
		} catch (IOException e) {
			Logger.error("JMX connection failed: " + e.getMessage());
			throw e;
		}


		try {
			jmxconmgmt.setMbsc(jmxconmgmt.getJmxcon().getMBeanServerConnection());
		} catch (IOException e) {
			Logger.error("JMX server connection failed: " + e.getMessage());
			throw e;
		}

		return jmxconmgmt;
	}


	private static Map<String, String[]> getJMXConnectionProperties() {
		Map<String, String[]> env = new Hashtable<String, String[]>();
		// If user and password is defined
		if (Bootstrap.getJMXProperties().getProperty("user") != null &&
				Bootstrap.getJMXProperties().getProperty("password") != null) {
			String[] credentials = new String[] {Bootstrap.getJMXProperties().getProperty("user"),
					Bootstrap.getJMXProperties().getProperty("password")};
			env.put(JMXConnector.CREDENTIALS, credentials);
		}
		return env;
	}


	private static JMXServiceURL getJMXUrl() throws MalformedURLException {
		JMXServiceURL jmxurl = null;
		try {

			jmxurl = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://" + 
					Bootstrap.getJMXProperties().getProperty("host") + 
					":" + 
					Bootstrap.getJMXProperties().getProperty("port") +  
			"/jmxrmi");

		} catch (MalformedURLException e1) {
			Logger.error("JMX service url is malformed: "+ e1.getMessage());
			throw e1;
		}
		return jmxurl;
	}

}
