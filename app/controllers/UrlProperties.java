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
import com.ingby.socbox.bischeck.xsd.urlservices.XMLUrlservices;
import com.ingby.socbox.bischeck.xsd.urlservices.XMLUrlproperty;

import models.*;

public class UrlProperties extends BasicController {
	
	public static XMLUrlservices getCache(){
		XMLUrlservices config = (XMLUrlservices) SessionData.getXMLConfig(ConfigXMLInf.XMLCONFIG.URL2SERVICES,XMLUrlservices.class);
	
		return config;
	}

	
    public static void list()  {
    	XMLUrlservices propertiesconfig = getCache();
    	
    	List<XMLUrlproperty> properties = propertiesconfig.getUrlproperty();
        
    	render(properties);
    }

    
    public static void edit(String key, String value) {
    	render(key,value);
    }
    
    
    public static void save(String key, String value) {
    	boolean existingKey = false;
    	XMLUrlservices propertiesconfig = getCache();
    	Iterator<XMLUrlproperty> propertiesIter = propertiesconfig.getUrlproperty().iterator();
    
    	while (propertiesIter.hasNext()) {	
        	XMLUrlproperty xmlprop = propertiesIter.next();
        	
        	if (xmlprop.getKey().equals(key)) {
        		existingKey = true;
        		xmlprop.setValue(value);
        	}
        }
    	
        if (!existingKey) {
        	List<XMLUrlproperty> propertiesList = propertiesconfig.getUrlproperty();
        	XMLUrlproperty property = new XMLUrlproperty();
        	property.setKey(key);
        	property.setValue(value);
        	propertiesList.add(property);
        }
        
        list();
    }
        
    
    public static void add() {
    	render();
    }

    
    public static void delete(String key) {
    	XMLUrlservices propertiesconfig = getCache();
    	List<XMLUrlproperty> propertiesList = propertiesconfig.getUrlproperty();
     	Iterator<XMLUrlproperty> propertiesIter = propertiesconfig.getUrlproperty().iterator();
        
    	while (propertiesIter.hasNext()) {
        	XMLUrlproperty xmlprop = propertiesIter.next();
        	
        	if (xmlprop.getKey().equals(key)) {
        		propertiesList.remove(xmlprop);
        		break;
        	}
        }
    	
        list();
    }
}