package controllers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;
import java.util.Vector;

import com.ingby.socbox.bischeck.serviceitem.ServiceItem;

public class Utils {

	public static List<String> getServiceItemClasses() {
		List<String> arr = new ArrayList<String>();
		arr.add("CalculateOnCache");
		arr.add("SQLServiceItem");
		arr.add("LivestatusServiceItem");
		return arr;
	}
}
