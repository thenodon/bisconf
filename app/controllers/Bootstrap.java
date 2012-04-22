package controllers;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;

import play.Logger;
import play.classloading.enhancers.ControllersEnhancer.ByPass;
import play.jobs.*;

@OnApplicationStart
public class Bootstrap extends Job {
    
	private static String bischeckversion = null;
    
	public void doJob() {
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		String path = null;
		
		if (System.getProperty("bishome") != null)
			path=System.getProperty("bishome");
		else {
			Logger.error("System property bishome must be set");
		}

		try {
			fstream = new FileInputStream(path + File.separator + "version.txt");

			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			bischeckversion = br.readLine();
			Logger.info("Bisheck version is " + bischeckversion);
		} catch (Exception ioe) {
			Logger.error("Can not determine the bischeck version");
		}
		finally {
			try {
				br.close();
			} catch (Exception ignore) {}
			try {
				in.close();
			} catch (Exception ignore) {}
			try {
				fstream.close();
			} catch (Exception ignore) {}	
		}

    }
    
	
	public static String getBischeckVersion() {
		return bischeckversion;
	}
	
}