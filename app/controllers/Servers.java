package controllers;

import play.*;
import play.cache.Cache;
import play.mvc.*;

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
    	render();
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
        		server.setClazz(classname);
        		edit(servername);
        	}
        }
    	// New 
    	XMLServer server = new XMLServer();
    	server.setName(servername);
    	server.setClazz(classname);
    	serversconfig.getServer().add(server);
    	edit(servername);
    }
    

    public static void edit(String servername) {
    	XMLServers serversconfig = getCache();
    	Iterator<XMLServer> servers = serversconfig.getServer().iterator();
        
    	while (servers.hasNext()){
        	XMLServer server = servers.next();
        	
        	if (server.getName().equals(servername)) {
        		render(server);
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
    	edit(servername);
    }

}