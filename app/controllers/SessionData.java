package controllers;

import play.Logger;
import play.cache.Cache;
import play.classloading.enhancers.ControllersEnhancer.ByPass;
import play.mvc.Controller;

import com.ingby.socbox.bischeck.ConfigFileManager;
import com.ingby.socbox.bischeck.ConfigXMLInf;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLBischeck;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperties;
import com.ingby.socbox.bischeck.xsd.servers.XMLServers;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLTwenty4Threshold;
import com.ingby.socbox.bischeck.xsd.urlservices.XMLUrlservices;
import com.ingby.socbox.bischeck.ValidateConfiguration;


public class SessionData extends BasicController {
	
	private static ConfigFileManager xmlfilemgr = new ConfigFileManager();

	/**
	 * Get the cached xml config data. If not existing init the cache data for the specific xmlconfig
	 * @param xmlconfig - for exampel ConfigXMLInf.XMLCONFIG.BISCHECK 
	 * @param clazz - for example XMLBischeck.class
	 * @return - the xml based object that must be casted for example to XMLBischeck 
	 */
	public static Object getXMLConfig(ConfigXMLInf.XMLCONFIG xmlconfig, Class clazz) {
		
		Object config = null;
	
		config = Cache.get(xmlconfig.nametag() + "_work_"+session.getId(),clazz);
		
		if (config == null){
			config = resetXMLConfig(xmlconfig, clazz);
		}
		
		return config; 
	}
	
	
	@ByPass
	public static void resetXMLConfigAll() {
		SessionData.resetXMLConfig(ConfigXMLInf.XMLCONFIG.BISCHECK,XMLBischeck.class);
		SessionData.resetXMLConfig(ConfigXMLInf.XMLCONFIG.PROPERTIES,XMLProperties.class);
		SessionData.resetXMLConfig(ConfigXMLInf.XMLCONFIG.TWENTY4HOURTHRESHOLD,XMLTwenty4Threshold.class);
		SessionData.resetXMLConfig(ConfigXMLInf.XMLCONFIG.URL2SERVICES,XMLUrlservices.class);
		SessionData.resetXMLConfig(ConfigXMLInf.XMLCONFIG.SERVERS,XMLServers.class);
		
	}
	
	@ByPass
	public static void resetXMLConfigAll(String repospath) {
		SessionData.resetXMLConfig(ConfigXMLInf.XMLCONFIG.BISCHECK,XMLBischeck.class,repospath);
		SessionData.resetXMLConfig(ConfigXMLInf.XMLCONFIG.PROPERTIES,XMLProperties.class,repospath);
		SessionData.resetXMLConfig(ConfigXMLInf.XMLCONFIG.TWENTY4HOURTHRESHOLD,XMLTwenty4Threshold.class,repospath);
		SessionData.resetXMLConfig(ConfigXMLInf.XMLCONFIG.URL2SERVICES,XMLUrlservices.class,repospath);
		SessionData.resetXMLConfig(ConfigXMLInf.XMLCONFIG.SERVERS,XMLServers.class,repospath);
		
	}
	
	@ByPass
	public static void saveXMLConfigAll(String repospath) throws Exception {
		SessionData.saveXMLConfig(ConfigXMLInf.XMLCONFIG.BISCHECK,repospath);
		SessionData.saveXMLConfig(ConfigXMLInf.XMLCONFIG.PROPERTIES,repospath);
		SessionData.saveXMLConfig(ConfigXMLInf.XMLCONFIG.TWENTY4HOURTHRESHOLD,repospath);
		SessionData.saveXMLConfig(ConfigXMLInf.XMLCONFIG.URL2SERVICES,repospath);
		SessionData.saveXMLConfig(ConfigXMLInf.XMLCONFIG.SERVERS,repospath);
		
		//ValidateConfiguration.verifyByDirectory(repospath);
	}
	
	
	/**
	 * 
	 * @param xmlconfig
	 * @param clazz
	 */
	private static Object resetXMLConfig(ConfigXMLInf.XMLCONFIG xmlconfig, Class clazz) {
		Object config = null;
		
		try {
			config = 
				 xmlfilemgr.getXMLConfiguration(xmlconfig);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Cache.delete(xmlconfig.nametag() + "_work_"+session.getId());
		Cache.set(xmlconfig.nametag() + "_work_"+session.getId(),config);
		return config;
	}
	/**
	 * 
	 * @param xmlconfig
	 * @param clazz
	 */
	private static Object resetXMLConfig(ConfigXMLInf.XMLCONFIG xmlconfig, Class clazz, String directory) {
		Object config = null;
		
		try {
			config = 
				 xmlfilemgr.getXMLConfiguration(xmlconfig, directory);
		} catch (Exception e) {
			Logger.error("Error reading xml configuration: "+ e.getMessage());
		}
		
		Cache.delete(xmlconfig.nametag() + "_work_"+session.getId());
		Cache.set(xmlconfig.nametag() + "_work_"+session.getId(),config);
		return config;
	}

	
	@ByPass
	private static void saveXMLConfig(ConfigXMLInf.XMLCONFIG xmlconfig, String directory) throws Exception {
		try {
			xmlfilemgr.createXMLFile(Cache.get(xmlconfig.nametag() + "_work_"+session.getId()), 
					xmlconfig, directory);
		} catch (Exception e) {
			Logger.error("Error creating xml configuration: "+ e.getMessage());
			throw e; 
		}
	}
	
}
