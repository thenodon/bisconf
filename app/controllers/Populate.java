package controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.ast.ClassLiteralAccess;

import play.Logger;
import play.jobs.*;

@OnApplicationStart
@Every("300s")
public class Populate extends Job {
	
	static List<String> clazzListServiceItem=new ArrayList<String>();
	static List<String> clazzListThreshold=new ArrayList<String>();
	static List<String> clazzListServer=new ArrayList<String>();
	
	private List<String> getClassList(String listfile) {
		String path = null;
    	if (System.getProperty("bisconfhome") != null)
			path=System.getProperty("bisconfhome");
		else {
			Logger.error("System property bisconfhome must be set");
		}
    	Properties prop = new Properties();
    	FileInputStream fileInput = null;
		
		File classfile = new File(path+File.separator+"etc",listfile);
	
		try {
			fileInput = new FileInputStream(classfile);
			prop.loadFromXML(fileInput);
		} catch (IOException ioe) {
			Logger.error("Can not read the class list file " + listfile);
		}
		Set<Object> propkeys = prop.keySet();
		List<String> list = new ArrayList(propkeys);
		return list;
	}
	
	
    public void doJob() {
    	
    	clazzListServiceItem = getClassList("serviceitemclasses.xml");
    	clazzListThreshold = getClassList("thresholdclasses.xml");
    	clazzListThreshold.add("");
    	clazzListServer = getClassList("serverclasses.xml");
	
    }
    

	public static List<String> getServiceItemClasses() {
		return clazzListServiceItem;
	}
	
	
	public static List<String> getThresholdClasses() {
		return clazzListThreshold;
	}
	

	public static List<String> getServerClasses() {
		return clazzListServer;
	}
	
}