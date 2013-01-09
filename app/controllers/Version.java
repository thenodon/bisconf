package controllers;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.management.RuntimeMXBean;
import java.net.MalformedURLException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.InvalidPropertiesFormatException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import com.ingby.socbox.bischeck.ConfigXMLInf;
import com.ingby.socbox.bischeck.ExecuteMBean;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLBischeck;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperties;
import com.ingby.socbox.bischeck.xsd.servers.XMLServers;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLTwenty4Threshold;

import play.Logger;
import play.i18n.Messages;
import sun.management.HotspotRuntimeMBean;

import models.ConfigMeta;
import models.User;




public class Version extends BasicController{


	private static File configPath() {
		String path = "";
		String xmldir;

		Map<String,String> bisprop = new HashMap<String, String>();

		
		JMXConnectionManagement jmxconnmgmt = null; 
		try {
			jmxconnmgmt = JMXConnectionManagement.createJMXConnection();
			getBischeckAttributes(bisprop, jmxconnmgmt.getMbeanServerConection());
		} catch (Exception e) {
			Logger.error("bishome catch - " + System.getProperty("bishome"));
			if (System.getProperty("bishome") != null)
				path=System.getProperty("bishome");
			else {
				Logger.error("System property bishome must be set");
			}

			if (System.getProperty("xmlconfigdir") != null) {
				xmldir=System.getProperty("xmlconfigdir");
			}else {
				xmldir="etc";
			}
		} finally {
			if (jmxconnmgmt != null)
				jmxconnmgmt.close();
		}

		Logger.error("bishome - " + bisprop.get("bishome"));


		if (bisprop.get("bishome")!= null) {
			path=bisprop.get("bishome");
		}
		else if (System.getProperty("bishome") != null) {
			path=System.getProperty("bishome");
		}
		else {
			Logger.error("System property bishome must be set");
		}

		if (bisprop.get("xmlconfigdir") != null) {
			xmldir=bisprop.get("xmlconfigdir");
		} else if (System.getProperty("xmlconfigdir") != null) {
			xmldir=System.getProperty("xmlconfigdir");
		}else {
			xmldir="etc";
		}

		File configDir = new File(path+File.separator+xmldir);
		if (configDir.isDirectory() && configDir.canRead()) { 
			Logger.info("Config directory is " + configDir.getAbsolutePath());
			return configDir;    
		}
		else {
			Logger.error("Configuration directory " + configDir.getPath() + " does not exist or is not readable.");
		}

		return null;
	}


	private static File reposPath()  {
		File configDir = new File(configPath()+File.separator + "repos");
		if (configDir.isDirectory() && configDir.canRead()) 
			return configDir;    
		else {
			boolean status = configDir.mkdir();
			if(!status) {
				Logger.error("Configuration directory " + configDir.getPath() + " does not exist or is not readable.");
			}
		}
		return configDir;
	}


	private static String now() {
		Calendar cal = GregorianCalendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(cal.getTime());
	}


	public static void list()  {

		List<ConfigMeta> versions = new ArrayList<ConfigMeta>(); 

		File[] files = null;

		initRepos("admin");

		try {
			files = reposPath().listFiles();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}

		String currentversion = getCurrentVersion();

		for (int i = 0; i< files.length; i++) {	 
			if(files[i].isDirectory()) {
				ConfigMeta metadata  = readMeta(files[i]);
				if (currentversion.equals(metadata.directory))
					metadata.current = true;
				versions.add(metadata);
			}
		}

		Collections.sort(versions);
		boolean started = isBischeckRunning();



		render(versions,started);

	}


	private static boolean isBischeckRunning() {

		JMXConnectionManagement jmxconnmgmt = null;
		try {
			jmxconnmgmt = JMXConnectionManagement.createJMXConnection();
			return true;
		} catch (Exception e) {
			return false;
		} finally {
			if(jmxconnmgmt != null)
				jmxconnmgmt.close();
		}

	}


	public static void add(String username) {
		render(username);
	}


	public static void select(String reposdir) {
		String repospath = null;
		try {
			repospath = reposPath().getAbsolutePath()+File.separator + reposdir;
		} catch (Exception e) {
			Logger.error("Could not get repos path");
		}

		SessionData.resetXMLConfigAll(repospath);

		Application.index();
	}


	public static void delete(String reposdir) {

		if (checkIfOwner(reposPath()+File.separator+reposdir)) {
			removeReposDir(reposPath()+File.separator+reposdir);
			flash.success(Messages.get("DeleteVersionSuccess"));
		} else {
			flash.error(Messages.get("DeleteVersionNotOwner"));
		}

		list();
	}


	private static boolean checkIfOwner(String repospath) {
		ConfigMeta metaconf = readMeta(new File(repospath));
		String username = session.get("username");

		User user = User.find("byUsername", username).first();

		if (username.equals(metaconf.username) || user.isAdmin) {
			return true;
		} else {
			return false;
		}
	}

	private static void removeReposDir(String repospath) {
		File repos = null;
		try {
			//repos = new File(reposPath(), reposdir);
			repos = new File(repospath);
		} catch (Exception e) {
			Logger.error("Could not get repos path");
		}

		// Empty directory
		File[] files = repos.listFiles();
		for (int i=0;i<files.length;i++) 
			files[i].delete();

		repos.delete();
	}



	private static ConfigMeta readMeta(File reposdir) {
		Properties properties = null;
		FileInputStream fileInput = null;
		String metapath = null;
		File metafile = new File(reposdir,"meta.xml"); 
		try {
			metapath = metafile.getAbsolutePath();

			if (metafile.exists()) {
				fileInput = new FileInputStream(metafile);
				properties = new Properties();
				properties.loadFromXML(fileInput);
			}	
		} 
		catch (InvalidPropertiesFormatException e) {
			Logger.error("Meta file " + metapath + " is not corrrect formated");
		} catch (Exception e) {
			Logger.error("Could not find meta file: " + e.getMessage());
		}
		finally {
			try {
				fileInput.close();
			} catch (IOException ignore) {}
		}
		ConfigMeta metadata = new ConfigMeta();
		metadata.created = properties.getProperty("created");
		metadata.directory = properties.getProperty("directory");
		metadata.username = properties.getProperty("username");
		metadata.comment = properties.getProperty("comment");

		return metadata;
	}


	private static void writeAndCreateMeta(File reposdir, ConfigMeta meta) {
		FileOutputStream metastream = null;

		try {
			if (!reposdir.exists())
				reposdir.mkdir();

			Properties prop = new Properties();
			prop.put("created", meta.created);
			prop.put("username", meta.username);
			prop.put("directory", meta.directory);
			prop.put("comment", meta.comment);

			File metafile = new File(reposdir,"meta.xml");
			metastream = new FileOutputStream(metafile);
			prop.storeToXML(metastream, "bisconf");			
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}
		finally {
			try {
				metastream.close();
			} catch (IOException ignore) {}			
		}
	}


	public static void deploy(String reposdir) {

		String username =  session.get("username");
		User user = User.find("byUsername", username).first();
		if (user.isAdmin || user.isDeploy) {

			File oldcurrent = new File(reposPath().getAbsolutePath()+File.separator + getCurrentVersion());
			File newcurrent = new File(reposPath().getAbsolutePath()+File.separator + reposdir);

			ConfigMeta metadata = readMeta(oldcurrent);
			metadata.current = false;
			writeAndCreateMeta(oldcurrent, metadata);

			copyAllXML(newcurrent, configPath());


			metadata = readMeta(newcurrent);
			metadata.current = true;
			writeAndCreateMeta(newcurrent, metadata);

			// Update the current file to with the new directory
			BufferedWriter metabuffer = null;

			try {
				metabuffer = new BufferedWriter(new FileWriter(configPath().getAbsolutePath()+File.separator + "current"));
				metabuffer.write(metadata.directory + "\n");

			} catch (Exception e) {
				Logger.error("Could not write ne current file");
			} 
			finally {
				try {
					metabuffer.close();
				} catch (IOException ignore) {}
			}

			/*
			 * Reload 
			 */
			if (isBischeckRunning()) {
				// Use jmx reload
				JMXConnectionManagement jmxconnmgmt = null;
				try {
					jmxconnmgmt = JMXConnectionManagement.createJMXConnection();
					ObjectName mbeanName;
					mbeanName = null;
				
					mbeanName = new ObjectName(ExecuteMBean.BEANNAME);

					ExecuteMBean mbeanProxy = JMX.newMBeanProxy(jmxconnmgmt.getMbeanServerConection(), mbeanName, 
							ExecuteMBean.class, true);
					if (mbeanProxy.reload()) {
						flash.success(Messages.get("ReloadSuccess"));
					} else {
						flash.success(Messages.get("ReloadFailed"));
					}
				}
				catch (Exception ioe) {
					flash.error(Messages.get("ReloadFailed"));
					Logger.error("Restarting bischeck failed with exception: " + ioe.getMessage());
				}
				finally {
					if(jmxconnmgmt != null)
						jmxconnmgmt.close();
				}
			}	
		} else {
			flash.error(Messages.get("UserIsNotAuthorized"));
		}
		list();
	}

	public static void startBischeckd() {
		try {
			manageBischeckd("start");
			flash.success(Messages.get("StartSucess"));
		} catch (Exception ex) {
			flash.error(Messages.get("StartFailed"), ex.getMessage());
		}
		status();
	}



	public static void restartBischeckd() {

		try {
			manageBischeckd("restart");
			flash.success(Messages.get("RestartSucess"));
		} catch (Exception ex) {
			flash.error(Messages.get("RestartFailed"), ex.getMessage());
		}

		status();

	}


	public static void stopBischeckd() {
		try {
			manageBischeckd("stop");
			flash.success(Messages.get("StopSucess"));
		} catch (Exception ex) {
			flash.error(Messages.get("StopFailed"), ex.getMessage());
		}
		status();
	}


	private static void manageBischeckd(String operation) throws Exception {

		String username =  session.get("username");
		User user = User.find("byUsername", username).first();
		if (user.isAdmin) {

			if (operation.equals("start") ||
					operation.equals("stop") ||
					operation.equals("restart") ) {
				Integer status = null;

				ProcessBuilder pb = new ProcessBuilder("sudo", "/etc/init.d/bischeckd", operation);

				try {
					Process p = pb.start();
					try {
						status = p.waitFor();
						if (status != 0) {
							Logger.error(operation +" " + Messages.get("BischeckFailded", String.valueOf(status)));
							throw new Exception(operation +" " + Messages.get("BischeckFailded", String.valueOf(status)));
						}
					} catch (InterruptedException ignore) {}

				} catch (IOException e) {
					Logger.error(operation + " " + Messages.get("BischeckFailded", e.getMessage()));
					throw new Exception(operation +" " + Messages.get("BischeckFailded", e.getMessage()));
				}

			}
			else {
				Logger.error(operation +" " + Messages.get("BischeckNotOperation"));
				throw new Exception(operation +" " + Messages.get("BischeckNotOperation"));
			}
		}
		else {
			throw new Exception(Messages.get("UserIsNotAuthorized"));
		}
	}



	/**
	 * Save the current working version
	 */
	public static void save(String username) {

		initRepos("admin");

		Long stamp = System.currentTimeMillis();

		File metadir = new File (reposPath().getAbsolutePath()+File.separator + stamp);
		ConfigMeta meta = new ConfigMeta();
		meta.comment = params.get("comment");
		meta.username = username;
		meta.created = now();
		meta.directory = ""+stamp;

		writeAndCreateMeta(metadir, meta);

		Bischeck.getCache();
		controllers.Properties.getCache();
		Twenty4Threshold.getCache();
		UrlProperties.getCache();
		Servers.getCache();

		try {
			SessionData.saveXMLConfigAll(metadir.getPath());
			flash.success(Messages.get("SaveVersionSuccess"));
		} catch (Exception e) {
			// Remove the repos directory if the saveXMLConfigAll failed in the
			// validation of any xml
			removeReposDir(metadir.getPath());
			flash.error(e.getMessage());
		}

		list();
	}

	private static void initRepos(String username) {
		File[] files = null;
		try {
			files = reposPath().listFiles();
		} catch (Exception e) {
			Logger.error(e.getMessage());
		}

		boolean foundversion = false;

		if (files != null) {
			for (int i = 0; i< files.length; i++) {
				File file = files[i];
				if(file.isDirectory())	
					if (new File(file.getAbsolutePath()+File.separator +"meta.xml").exists()) {
						foundversion = true;
						return;
					}
			}
		}

		// Init if empty
		if (!foundversion) {
			Long stamp = System.currentTimeMillis();
			FileOutputStream metastream = null;
			BufferedWriter currentbuffer = null;
			try {
				File first = new File (reposPath().getAbsolutePath()+File.separator + stamp);
				first.mkdir();
				copyAllXML(configPath(), first);

				Properties prop = new Properties();
				prop.put("created", now());
				prop.put("username", username);
				prop.put("directory", ""+stamp);
				prop.put("comment", "Inital version");


				File file = new File(reposPath().getAbsolutePath()+File.separator + stamp +File.separator +"meta.xml");
				metastream = new FileOutputStream(file);
				prop.storeToXML(metastream, "Inital");

				currentbuffer = new BufferedWriter(new FileWriter(configPath().getAbsolutePath()+File.separator + "current"));
				currentbuffer.write(stamp + "\n");

			} catch (Exception e) {
				Logger.error(e.getMessage());
			}
			finally {
				try {
					metastream.close();
				} catch (IOException ignore) {}
				try {
					currentbuffer.close();
				} catch (IOException ignore) {}
			}
		}
	}


	private static String getCurrentVersion() {
		FileInputStream fstream = null;
		String strLine = null;
		DataInputStream in = null;
		BufferedReader br = null;

		try {
			fstream = new FileInputStream(configPath().getAbsolutePath()+File.separator + "current");

			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			strLine = br.readLine();
		} catch (Exception ioe) {

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
		return strLine;
	}


	private static final String IMAGE_PATTERN = 
		"([^\\s]+(\\.(?i)(xml))$)";


	public static boolean validate(final String file){
		Pattern pattern;
		Matcher matcher;
		pattern = Pattern.compile(IMAGE_PATTERN);
		matcher = pattern.matcher(file);
		return matcher.matches();
	}


	private static void copyAllXML(File fromdir, File todir){
		File[] files = null;
		files = fromdir.listFiles();
		for (int i = 0; i< files.length; i++) {
			if (validate(files[i].getName())){
				copyFile(files[i], new File(todir.getAbsolutePath() + File.separator + files[i].getName()));
			}
		}	
	}


	private static void copyFile(File sourceFile, File destFile) {
		if(!destFile.exists()) {
			try {
				destFile.createNewFile();
			} catch (IOException e) {
				Logger.error("Can not create destination file " + 
						destFile.getAbsolutePath() + 
						" with exception: " + 
						e.getMessage());
			}
		}

		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		}
		catch (IOException ioe) {
			Logger.error("Can not copy file: " + ioe.getMessage());
		}
		finally {
			if(source != null) {
				try {
					source.close();
				} catch (IOException ignore) {}
			}
			if(destination != null) {
				try {
					destination.close();
				} catch (IOException ignore) {}
			}
		}
	}

	public static void status()  {

		Map<String,String> bisprop = new HashMap<String, String>();

		//Set default from system property
		bisprop.put("bishome",System.getProperty("bishome"));
		bisprop.put("xmlconfigdir",System.getProperty("xmlconfigdir",""));

		//throw new Exception("TEST error");

		// JMX based
		JMXConnectionManagement jmxconnmgmt = null;
		try {
			jmxconnmgmt = JMXConnectionManagement.createJMXConnection(); 
			getJVMAttributes(bisprop,jmxconnmgmt.getMbeanServerConection());
			getBischeckAttributes(bisprop, jmxconnmgmt.getMbeanServerConection());
		}
		catch (Exception ioe) {
			bisprop.put("pid", "not running or no JMX connection");
		}
		finally {
			if (jmxconnmgmt != null)
				jmxconnmgmt.close();
		}


		render(bisprop);

	}

/*
	private static MBeanServerConnection createMBeanServerConnection() throws Exception {
		JMXServiceURL u = null;
		JMXConnector c = null;
		MBeanServerConnection mbsc = null;


		try {

			u = new JMXServiceURL(
					"service:jmx:rmi:///jndi/rmi://" + 
					Bootstrap.getJMXProperties().getProperty("host") + 
					":" + 
					Bootstrap.getJMXProperties().getProperty("port") +  
			"/jmxrmi");

		} catch (MalformedURLException e1) {
			Logger.error("JMX service url is malformed: "+ e1.getMessage());
			throw e1;
		}

		Map<String, String[]> env = new Hashtable<String, String[]>();
		// If user and password is defined
		if (Bootstrap.getJMXProperties().getProperty("user") != null &&
				Bootstrap.getJMXProperties().getProperty("password") != null) {
			String[] credentials = new String[] {Bootstrap.getJMXProperties().getProperty("user"),
					Bootstrap.getJMXProperties().getProperty("password")};
			env.put(JMXConnector.CREDENTIALS, credentials);
		}

		try {
			c = JMXConnectorFactory.connect(u,env);
		} catch (IOException e) {
			Logger.error("JMX connection failed: " + e.getMessage());
			throw e;
		}


		try {
			mbsc = c.getMBeanServerConnection();
		} catch (IOException e) {
			Logger.error("JMX server connection failed: " + e.getMessage());
			throw e;
		}

		return mbsc;
	}
*/

	private static void getJVMAttributes(Map<String, String> bisprop, MBeanServerConnection mbsc) throws MalformedObjectNameException, NullPointerException {
		ObjectName mbeanName = null;
		mbeanName = new ObjectName("java.lang:type=Runtime");

		RuntimeMXBean mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, 
				RuntimeMXBean.class, true);

		bisprop.put("uptime",(new Date(mbeanProxy.getStartTime()).toString()));
		bisprop.put("pid",mbeanProxy.getName().substring(0,mbeanProxy.getName().indexOf('@')));
	}


	private static void getBischeckAttributes(Map<String, String> bisprop,
			MBeanServerConnection mbsc) throws MalformedObjectNameException {

		ObjectName mbeanName;
		mbeanName = null;
		mbeanName = new ObjectName("com.ingby.socbox.bischeck:name=Execute");

		ExecuteMBean mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, 
				ExecuteMBean.class, true);
		if (mbeanProxy.getReloadTime() == null)
			bisprop.put("reloadtime","NA");
		else
			bisprop.put("reloadtime",(new Date(mbeanProxy.getReloadTime()).toString()));
		bisprop.put("reloadcount",mbeanProxy.getReloadCount().toString());
		bisprop.put("bishome",mbeanProxy.getBischeckHome());
		bisprop.put("xmlconfigdir",mbeanProxy.getXmlConfigDir());
		bisprop.put("bischeckversion",mbeanProxy.getBischeckVersion());

	}
}
