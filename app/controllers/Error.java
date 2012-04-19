package controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import models.User;
import play.mvc.Before;
import play.mvc.Controller;

public class Error extends Controller{
	
	@Before
    static void setConnectedUser() {
        if(Security.isConnected()) {
            User user = User.find("byUsername", Security.connected()).first();
            renderArgs.put("user", user);
        }
    }
	
	 
	public static void show(String message, String stacktrace) {
		render(message,stacktrace);
	}
	
	
}
