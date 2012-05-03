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




public class Version extends BasicController{


	private static File configPath() {
		String path = "";
		String xmldir;

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

		File configDir = new File(path+File.separator+xmldir);
		if (configDir.isDirectory() && configDir.canRead()) 
			return configDir;    
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

		if (Bootstrap.getBischeckVersion().equals("0.3.3")) {

			ProcessBuilder pb = new ProcessBuilder("sudo", "/etc/init.d/bischeckd", "pidstatus");
			InputStream is = null;
			InputStreamReader isr = null;
			BufferedReader br = null;
			try {
				Process p;
				try {
					p = pb.start();

					is = p.getInputStream();
					isr = new InputStreamReader(is);
					br = new BufferedReader(isr);
					
					String line = br.readLine();
					if (line == null)
						return false;
					
					if (line.equals("not running")) 
						return false;
					else 
						return true;

				} catch (IOException e) {
					return false;
				}
			} finally {
				try {
					br.close();
				} catch (IOException ignore) {}
				try {
					isr.close();
				} catch (IOException ignore) {}
				try {
					is.close();
				} catch (IOException ignore) {}

			}
		}
		else {
			try {
				createMBeanServerConnection();
				return true;
			} catch (Exception e) {
				return false;
			}

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
		File repos = null;
		try {
			repos = new File(reposPath(), reposdir);
		} catch (Exception e) {
			Logger.error("Could not get repos path");
		}

		// Empty directory
		File[] files = repos.listFiles();
		for (int i=0;i<files.length;i++) 
			files[i].delete();

		repos.delete();
		flash.success(Messages.get("DeleteVersionSuccess"));
		list();
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
		 * REstart 
		 */
		Integer status = null;
		if (Bootstrap.getBischeckVersion().equals("0.3.3")) {
			// Use script restart
			ProcessBuilder pb = new ProcessBuilder("sudo", "/etc/init.d/bischeckd", "restart");

			try {
				Process p = pb.start();
				try {
					status = p.waitFor();
					if (status != 0) {
						Logger.error("Restarting bischeck failed with return status " + status);
					}
				} catch (InterruptedException ignore) {}

			} catch (IOException e) {
				Logger.error("Restarting bischeck failed with exception: " + e.getMessage());
			}
		} else {
			// Use jmx reload
			try {
				MBeanServerConnection mbsc = createMBeanServerConnection();
				ObjectName mbeanName;
				mbeanName = null;
				//mbeanName = new ObjectName("com.ingby.socbox.bischeck:name=Execute");
				mbeanName = new ObjectName(ExecuteMBean.BEANNAME);

				ExecuteMBean mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, 
						ExecuteMBean.class, true);
				mbeanProxy.reload();

			}
			catch (Exception ioe) {
				Logger.error("Restarting bischeck failed with exception: " + ioe.getMessage());
			}
		}		

		list();
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

		if ( !("0.3.3").equals(Bootstrap.getBischeckVersion()))
			Servers.getCache();

		SessionData.saveXMLConfigAll(metadir.getPath());
		flash.success(Messages.get("SaveVersionSuccess"));
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

	public static void status() throws Exception {

		Map<String,String> bisprop = new HashMap<String, String>();
		bisprop.put("bischeckversion",Bootstrap.getBischeckVersion());

		//throw new Exception("TEST error");

		if (Bootstrap.getBischeckVersion().equals("0.3.3")) {
			// Use script restart
			getNativeAttributes(bisprop);

		} else {
			// JMX based
			try {
				MBeanServerConnection mbsc = createMBeanServerConnection();
				getJVMAttributes(bisprop,mbsc);
				getBischeckAttributes(bisprop, mbsc);
			}
			catch (IOException ioe) {
				bisprop.put("pid", "Not started");
			}


		}
		render(bisprop);

	}


	private static void getNativeAttributes(Map<String, String> bisprop) throws IOException {
		Integer status;
		ProcessBuilder pb = new ProcessBuilder("sudo", "/etc/init.d/bischeckd", "pidstatus");
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;
		try {
			Process p = pb.start();
			is = p.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);
			
			String pid = br.readLine();
			if (pid != null)
				bisprop.put("pid",pid);
			else
				bisprop.put("pid","not running");
			System.out.println();
			try {
				status = p.waitFor();
				if (status != 0) {
					Logger.error("Getting bischeck pid failed with return status " + status);
				}
			} catch (InterruptedException ignore) {}
		} finally {
			try {
				br.close();
			} catch (IOException ignore) {}
			try {
				isr.close();
			} catch (IOException ignore) {}
			try {
				is.close();
			} catch (IOException ignore) {}

		}
		
		if (!bisprop.get("pid").equals("not running")) {
			pb = new ProcessBuilder("ps", "--no-headers", "-ostime",bisprop.get("pid"));
			try {
				Process p = pb.start();
				is = p.getInputStream();
				isr = new InputStreamReader(is);
				br = new BufferedReader(isr);

				String uptime = br.readLine();
				if (uptime != null)
					bisprop.put("uptime",uptime);
				else
					bisprop.put("uptime","N/A");
				try {
					status = p.waitFor();
				} catch (InterruptedException ignore) {}


			} finally {
				try {
					br.close();
				} catch (IOException ignore) {}
				try {
					isr.close();
				} catch (IOException ignore) {}
				try {
					is.close();
				} catch (IOException ignore) {}

			}
		}
		else
			bisprop.put("uptime","N/A");
	}


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

		try {
			c = JMXConnectorFactory.connect(u);
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


	private static void getJVMAttributes(Map<String, String> bisprop, MBeanServerConnection mbsc) throws MalformedObjectNameException, NullPointerException {
		ObjectName mbeanName = null;
		mbeanName = new ObjectName("java.lang:type=Runtime");

		RuntimeMXBean mbeanProxy = JMX.newMBeanProxy(mbsc, mbeanName, 
				RuntimeMXBean.class, true);

		bisprop.put("uptime",(new Date(mbeanProxy.getStartTime()).toString()));
		bisprop.put("pid",mbeanProxy.getName().substring(0,mbeanProxy.getName().indexOf('@')));
	}


	private static void getBischeckAttributes(Map<String, String> bisprop,
			MBeanServerConnection mbsc) throws MalformedObjectNameException, NullPointerException {
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

	}
}
