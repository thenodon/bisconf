package controllers;
 

import com.ingby.socbox.bischeck.ConfigXMLInf;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLBischeck;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperties;

import models.*;
 
public class Security extends Secure.Security {
 
    static boolean authentify(String username, String password) {
        return User.connect(username, password) != null;
    }
    
    static boolean check(String profile) {
        if("admin".equals(profile)) {
            return User.find("byUsername", connected()).<User>first().isAdmin;
        }
        return false;
    }

    
    static void onAuthenticated() {
    	Application.resetAll();
    	Application.index();
    }
    
}