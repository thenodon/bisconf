package controllers;

import play.*;
import play.cache.Cache;
import play.i18n.Messages;
import play.mvc.*;

import java.util.*;

import com.ingby.socbox.bischeck.ConfigFileManager;
import com.ingby.socbox.bischeck.ConfigXMLInf;
import com.ingby.socbox.bischeck.ConfigurationManager;
import com.ingby.socbox.bischeck.ConfigXMLInf.XMLCONFIG;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLBischeck;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperties;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperty;

import models.*;

public class Properties extends BasicController {
	
	@Before
	static void setConnectedUser() {
		if(Security.isConnected()) {
			User user = User.find("byUsername", Security.connected()).first();
			if (!user.isAdmin) {
				flash.error(Messages.get("UserIsNotAuthorized"));
				Application.index();
				
			}
		}
	}
	
	
	public static XMLProperties getCache(){
		XMLProperties config = (XMLProperties) SessionData.getXMLConfig(ConfigXMLInf.XMLCONFIG.PROPERTIES,XMLProperties.class);
	
		return config;
	}

	
    public static void list()  {
    	XMLProperties propertiesconfig = getCache();
    	List<XMLProperty> properties = propertiesconfig.getProperty();
        
    	render(properties);
    }

    
    public static void edit(String key, String value) {
    	render(key,value);
    }
    
    
    public static void save(String key, String value) {
    	boolean existingKey = false;
    	XMLProperties propertiesconfig = getCache();
    	Iterator<XMLProperty> propertiesIter = propertiesconfig.getProperty().iterator();
        while (propertiesIter.hasNext()) {
        	
        	XMLProperty xmlprop = propertiesIter.next();
        	if (xmlprop.getKey().equals(key)) {
        		existingKey = true;
        		xmlprop.setValue(value);
        	}
        }
        if (!existingKey) {
        	List<XMLProperty> propertiesList = propertiesconfig.getProperty();
        	XMLProperty property = new XMLProperty();
        	property.setKey(key);
        	property.setValue(value);
        	propertiesList.add(property);
        }
        flash.success(Messages.get("SavePropertySuccess"));
        list();
    }
    
    
    public static void add() {
    	render();
    }

    
    public static void delete(String key) {
    	XMLProperties propertiesconfig = getCache();
    	List<XMLProperty> propertiesList = propertiesconfig.getProperty();
     	Iterator<XMLProperty> propertiesIter = propertiesconfig.getProperty().iterator();
        
    	while (propertiesIter.hasNext()) {
        	XMLProperty xmlprop = propertiesIter.next();
        	
        	if (xmlprop.getKey().equals(key)) {
        		propertiesList.remove(xmlprop);
        		break;
        	}
        }
    	flash.success(Messages.get("DeletePropertySuccess"));
        list();
    }
    
}