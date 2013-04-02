package controllers;
 
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import models.User;

import com.ingby.socbox.bischeck.ConfigXMLInf;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLBischeck;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperties;
import com.ingby.socbox.bischeck.xsd.servers.XMLServers;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLTwenty4Threshold;

import controllers.Secure;

import play.Logger;
import play.i18n.Messages;
import play.mvc.*;
 
public class Application extends BasicController {
 
	private static final String EMAIL_PATTERN = 
			"^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
			+ "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
	private static Pattern pattern = Pattern.compile(EMAIL_PATTERN);
		
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
				
				if (validateEmail(params.get("email"))){
					user.email = params.get("email");
				} else {
					flash.error(Messages.get("NotACorrectEmail"));
					editUser();
				}
				
				if (params.get("password1").length() != 0 && 
					params.get("password1").equals(params.get("password2")) ) {
						user.password = params.get("password1");
						user.save();
						flash.success(Messages.get("UserUpdated"));
				} else {
					flash.error(Messages.get("PasswordNotEqual"));
					editUser();
				}
				
				
			}
		}
		index();
	}
	
	private static boolean validateEmail(final String email) {
		 
		Matcher matcher = pattern.matcher(email);
		return matcher.matches();
 
	}
	
	public static void about() {
		String bisconfversion = Bootstrap.getBisconfVersion();
		render(bisconfversion);
	}
}