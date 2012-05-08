package controllers;
 
import models.User;

import com.ingby.socbox.bischeck.ConfigXMLInf;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLBischeck;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperties;
import com.ingby.socbox.bischeck.xsd.servers.XMLServers;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLTwenty4Threshold;

import controllers.Secure;

import play.Logger;
import play.mvc.*;
 
public class Application extends BasicController {
 
	
	public static void index() {
		if (session.get("username") == null) {
			try {
				Secure.logout();
			} catch (Throwable e) {	
				Logger.error("Secure throw exception: " + e.getMessage());
			}
		}	
		
		render();
	}
	
	/*
	public static void index(String status) {
		render(status);
	}
	*/
	
	public static void resetAll() {
		SessionData.resetXMLConfigAll();
    	Application.index();
    }
	public static void editUser() {
		User user = User.find("byUsername", Security.connected()).first();
		render(user);
	}
	
	public static void saveUser() {
		if(Security.isConnected()) {
			if (session.get("username").equals(params.get("username"))) {
			
				User user = User.find("byUsername", Security.connected()).first();
				user.email = params.get("email");
				if (params.get("password1").length()!=0)
					user.password = params.get("password1");
				user.save();
			}
		}
		index();
	}
	
	public static void about() {
		String bisconfversion = Bootstrap.getBisconfVersion();
		render(bisconfversion);
	}
}