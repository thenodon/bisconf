package controllers;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import play.Logger;
import play.classloading.enhancers.ControllersEnhancer.ByPass;
import play.jobs.*;

@OnApplicationStart
public class Bootstrap extends Job {
    
	private static String bischeckversion = null;
	private static String bisconfversion = null;
	private static Properties jmxproperties = null;
    
	public void doJob() {
		bischeckversion = readBischeckVersion();
		bisconfversion = readBisconfVersion();
		jmxproperties = readJMXProperties();	
    }

	private static String readBischeckVersion() {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		String path = null;
		
		if (System.getProperty("bishome") != null)
			path=System.getProperty("bishome");
		else {
			Logger.error("System property bishome must be set");
		}

		try {
			fstream = new FileInputStream(path + File.separator + "version.txt");

			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			bischeckversion = br.readLine();
			Logger.info("Bisheck version is " + bischeckversion);
		} catch (Exception ioe) {
			bischeckversion = "N/A";
			Logger.error("Can not determine the bischeck version");
		}
		finally {
			try {
				br.close();
			} catch (Exception ignore) {}
			try {
				in.close();
			} catch (Exception ignore) {}
			try {
				fstream.close();
			} catch (Exception ignore) {}	
		}
		return bischeckversion;
	}
    
	private static String readBisconfVersion() {
		
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		String path = null;
		
		if (System.getProperty("bisconfhome") != null)
			path=System.getProperty("bisconfhome");
		else {
			Logger.error("System property bisconfhome must be set");
		}

		try {
			fstream = new FileInputStream(path + File.separator + "version.txt");

			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			bisconfversion = br.readLine();
			Logger.info("Bisheck version is " + bisconfversion);
		} catch (Exception ioe) {
			bisconfversion = "N/A";
			Logger.error("Can not determine the bischeck version");
		}
		finally {
			try {
				br.close();
			} catch (Exception ignore) {}
			try {
				in.close();
			} catch (Exception ignore) {}
			try {
				fstream.close();
			} catch (Exception ignore) {}	
		}
		return bisconfversion;
	}

	
	private static Properties readJMXProperties() {
		String path = null;
    	if (System.getProperty("bisconfhome") != null)
			path=System.getProperty("bisconfhome");
		else {
			Logger.error("System property bisconfhome must be set");
		}
    	Properties prop = new Properties();
    	FileInputStream fileInput = null;
		
		File classfile = new File(path+File.separator+"etc","jmx.xml");
	
		try {
			fileInput = new FileInputStream(classfile);
			prop.loadFromXML(fileInput);
		} catch (IOException ioe) {
			Logger.error("Can not read the jmx.xml - set values to default");
			prop.setProperty("host", "localhost");
			prop.setProperty("port", "3333");
		}
		return prop;
	
	}
	
	public static String getBischeckVersion() {
		return bischeckversion;
	}
	
	public static String getBisconfVersion() {
		return bisconfversion;
	}
	
	public static Properties getJMXProperties() {
		return jmxproperties;
	}
}