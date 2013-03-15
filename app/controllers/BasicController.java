package controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import models.User;
import play.Logger;
import play.cache.Cache;
import play.classloading.enhancers.ControllersEnhancer.ByPass;
import play.mvc.Before;
import play.mvc.Catch;
import play.mvc.Controller;

public class BasicController extends Controller{

	private static final int MAXSTRINGSIZE = 2048;

	
	@Before
	static void setConnectedUser() {
		if(Security.isConnected()) {
			User user = User.find("byUsername", Security.connected()).first();
			renderArgs.put("user", user);
		}
	}

	@Before 
	static void checkNewVersion() {
		if (!Cache.get("lastVersionId").equals(session.get("lastVersionId"))) {
			Logger.debug("Check - session - lastVersionId " + session.get("lastVersionId"));
			Logger.debug("Check - Cache - lastVersionId " + Cache.get("lastVersionId"));
			Logger.debug("Check - Cache - lastUserId " + Cache.get("lastUserId"));
			
			renderArgs.put("newVersion", true);	
			renderArgs.put("newVersionUser", Cache.get("lastUserId"));
		} else {
			renderArgs.put("newVersion", false);
		}
		
	}
	
	//@Before
	/*static void getBischeckVersion() {
		renderArgs.put("bischeckversion", Bootstrap.getBischeckVersion());
	}
*/
	
	@ByPass
	@Catch (value = Throwable.class, priority = 1)
	public static void logThrowable(Throwable throwable) {
		// Custom error logging…
		Logger.error("Exp: %s", throwable);
		// Check if there is a better way then truncate.
		if (throwable.toString().length() > MAXSTRINGSIZE)
			Error.show(throwable.toString(),getStackTrace(throwable).substring(0, MAXSTRINGSIZE));
		else
			Error.show(throwable.toString(),getStackTrace(throwable));
	}

	private static String getStackTrace(Throwable aThrowable) {
		Writer result = null;
		PrintWriter printWriter = null;
		
		result = new StringWriter();
		printWriter = new PrintWriter(result);
		aThrowable.printStackTrace(printWriter);			
		
		return result.toString();
	}
}
