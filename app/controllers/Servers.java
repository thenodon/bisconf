package controllers;

import play.*;
import play.cache.Cache;
import play.i18n.Messages;
import play.mvc.*;

import java.lang.reflect.Method;
import java.util.*;

import com.ingby.socbox.bischeck.ConfigFileManager;
import com.ingby.socbox.bischeck.ConfigXMLInf;
import com.ingby.socbox.bischeck.ConfigurationManager;
import com.ingby.socbox.bischeck.ConfigXMLInf.XMLCONFIG;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLBischeck;

import com.ingby.socbox.bischeck.xsd.servers.XMLProperty;
import com.ingby.socbox.bischeck.xsd.servers.XMLServer;
import com.ingby.socbox.bischeck.xsd.servers.XMLServers;

import models.*;

public class Servers extends BasicController {

	
	public static XMLServers getCache(){
		XMLServers config = (XMLServers) SessionData.getXMLConfig(ConfigXMLInf.XMLCONFIG.SERVERS,XMLServers.class);
	
		return config;
	}

	
    public static void list()  {
    	XMLServers serversconfig = getCache();
    	List<XMLServer> servers = serversconfig.getServer();
        
    	render(servers);
    }
    
    
    public static void add() {
    	List<String> serverclasses = Populate.getServerClasses();
    	render(serverclasses);
    }
    
    
    public static void delete(String servername) {
    	XMLServers serversconfig = getCache();
    	Iterator<XMLServer> servers = serversconfig.getServer().iterator();
        
    	while (servers.hasNext()){
        	XMLServer server = servers.next();
        	
        	if (server.getName().equals(servername)) {
        		serversconfig.getServer().remove(server);
        		break;
        	}
    	}
    	flash.success(Messages.get("DeleteServerSuccess"));
    	list();
    }

    
    public static void save() {
    	String servername = params.get("servername");
    	String classname = params.get("classname");
    	
    	XMLServers serversconfig = getCache();
    	Iterator<XMLServer> servers = serversconfig.getServer().iterator();
        
    	while (servers.hasNext()){
        	XMLServer server = servers.next();
        	
        	if (server.getName().equals(servername)) {
        		if (!params.get("classname").equals(params.get("oldclassname"))) {
        			server.setClazz(classname);
        			/////////////////
        			// Replace server properties
        			replaceServerProperties(server);
        			
            		/////////////////
        			flash.success(Messages.get("SaveServerSuccess"));
        		}
        		edit(servername);
        	}
        }
    	// New 
    	validation.required(servername);
			
    	if (validation.hasErrors()) {
    		// Used to identify the period that had the error
    		validation.keep();
    		add();
    	}
    	
    	XMLServer server = new XMLServer();
    	server.setName(servername);
    	server.setClazz(classname);
    	serversconfig.getServer().add(server);
    	flash.success(Messages.get("SaveServerSuccess"));
    	edit(servername);
    }
    
    private static boolean replaceServerProperties(XMLServer server) {
    	Class<?> serverclazz; 
		
    	try {
    		serverclazz = Class.forName(server.getClazz());
    	} catch (ClassNotFoundException e) {
    		try {
    			serverclazz = Class.forName("com.ingby.socbox.bischeck.servers." + 
    					server.getClazz());
    		} catch (Exception ret) {
    			return false;
    		}
    	}

    	java.util.Properties defaultproperties = null;
    	Method method;
    	try {
    		method = serverclazz.getMethod("getServerProperties");
    		defaultproperties  = (java.util.Properties) method.invoke(null);
    	} catch (Exception ret) {
    		return false;
    	}



    	Iterator<XMLProperty> iter = server.getProperty().iterator();
    	// Update the default properties with what is currently set 
    	// in the server property
    	while (iter.hasNext()){
    		XMLProperty xmlprop = iter.next();
    		if(defaultproperties.containsKey(xmlprop.getKey()))
    			defaultproperties.setProperty(xmlprop.getKey(), xmlprop.getValue());
    	}

    	// Create a new server property list
    	List<XMLProperty>serverproperty = new ArrayList<XMLProperty>();
    	Iterator<Object> keyiter = defaultproperties.keySet().iterator();

    	while (keyiter.hasNext()){
    		String key = (String) keyiter.next();
    		XMLProperty xmlprop = new XMLProperty();
    		xmlprop.setKey(key);
    		xmlprop.setValue((String) defaultproperties.get(key));
    		serverproperty.add(xmlprop);
    	}

    	server.getProperty().clear();
    	server.getProperty().addAll(serverproperty);
    	
    	return true;
    }

    /**
     * If the server class has a static method called gerServerProperties 
     * the default properties will be used to create the list with default 
     * values. If this is the case the gui will not allow deletion of properties 
     * or allowing to add additional properties.
     * If the server class do not have any  
     * @param servername
     */
    public static void edit(String servername) {
    	XMLServers serversconfig = getCache();
    	Iterator<XMLServer> servers = serversconfig.getServer().iterator();
        
    	while (servers.hasNext()){
        	XMLServer server = servers.next();
        	
        	if (server.getName().equals(servername)) {
        		
        		List<String> serverclasses = Populate.getServerClasses();
        		
        		boolean hasdefaultproperties = replaceServerProperties(server);
               		
        		render(server, serverclasses, hasdefaultproperties);
        	}
    	}
    	render();
    }
    
    
    public static void addProperty(String servername) {
    	render(servername);
    }
    
    
    public static void editProperty(String servername, String key, String value) {
    	render(servername, key, value);
    }
    
    
    public static void saveProperty() {
    	String servername = params.get("servername");
    	String key = params.get("key");
    	String value = params.get("value");
    	
    	boolean existingKey = false;
    	XMLServers serversconfig = getCache();
    	Iterator<XMLServer> servers = serversconfig.getServer().iterator();
        
    	while (servers.hasNext()){
        	XMLServer server = servers.next();
        	
        	if (server.getName().equals(servername)) {
        
        		Iterator<XMLProperty> propertiesIter = server.getProperty().iterator();
        		while (propertiesIter.hasNext()) {
        	
        			XMLProperty xmlprop = propertiesIter.next();
        			
        			if (xmlprop.getKey().equals(key)) {
        				existingKey = true;
        				xmlprop.setValue(value);
        			}
        		}
        		if (!existingKey) {
        			List<XMLProperty> propertiesList = server.getProperty();
        			XMLProperty property = new XMLProperty();
        			property.setKey(key);
        			property.setValue(value);
        			propertiesList.add(property);
        		}
        		flash.success(Messages.get("SaveServerPropertySuccess"));
        		edit(server.getName());
        	}
    	}
    }
    
    
    public static void deleteProperty(String servername, String key) {
    
    	XMLServers serversconfig = getCache();
    	Iterator<XMLServer> servers = serversconfig.getServer().iterator();
        
    	while (servers.hasNext()){
    		XMLServer server = servers.next();
        	
        	if (server.getName().equals(servername)) {
        		Iterator<XMLProperty> propertiesIter = server.getProperty().iterator();
        		while (propertiesIter.hasNext()) {
        	
        			XMLProperty xmlprop = propertiesIter.next();
        			if (xmlprop.getKey().equals(key)) {
        				server.getProperty().remove(xmlprop);
        				break;
        			}
        		}
        	}
    	}	
    	flash.success(Messages.get("DeleteServerPropertySuccess"));
    	edit(servername);
    }

}