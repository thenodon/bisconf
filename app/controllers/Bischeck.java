package controllers;

import play.*;
import play.cache.Cache;
import play.data.validation.Error;
import play.data.validation.Validation.Validator;
import play.i18n.Messages;
import play.mvc.*;

import java.util.*;

import org.quartz.CronExpression;

import com.ingby.socbox.bischeck.ConfigFileManager;
import com.ingby.socbox.bischeck.ConfigXMLInf;
import com.ingby.socbox.bischeck.ConfigurationManager;
import com.ingby.socbox.bischeck.ConfigXMLInf.XMLCONFIG;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLBischeck;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLHost;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLService;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLServiceitem;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperties;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperty;

import models.*;

@With(Secure.class)
public class Bischeck extends BasicController {
	
	
	private static final String VALIDNAMEREGEX = "[a-zA-Z0-9]+";


	public static XMLBischeck getCache(){
		XMLBischeck config = (XMLBischeck) SessionData.getXMLConfig(ConfigXMLInf.XMLCONFIG.BISCHECK,XMLBischeck.class);
	
		return config;
	}
	
	/*************************************
	 *
	 * Host section
	 * 
	 *************************************/
	
	public static void listhosts()  {
		XMLBischeck config = getCache();
		
		List<XMLHost> hosts = config.getHost();

		render(hosts);
	}
	
	
	public static void deletehost(String hostname) {
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname)) {
				config.getHost().remove(host);
				flash.success(Messages.get("DeleteHostSuccess"));
				listhosts();
			}
		}
	}

	
	public static void edithost(String hostname) {
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname)) {
				render(host);
			}
		}
		render();
	}

	
	/**
	 * Add a new host to bischeck
	 */
	public static void addhost() {
		XMLHost host = new XMLHost();
		host.setName("");
		host.setDesc("");
		render(host);
	}

	
	/**
	 * Save change to the host attributes. If this is an existing host send it to
	 * listhost. If its a new host send it to addservice.
	 */
	public static void savehost() {	
		/*
		 * Check if already exist
		 * Check format - One string
		 */
		XMLBischeck config = getCache();
		
		Iterator<XMLHost> hosts = config.getHost().iterator();
		// Check if an existing host - update
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(params.get("oldname"))) {
				String name = params.get("name");
				validation.required(name);
				validation.match(name,VALIDNAMEREGEX);
				if (validation.hasErrors()) {
					// Used to identify the period that had the error
					validation.keep();
					edithost(params.get("oldname"));
				}
				else { 
				host.setName(params.get("name"));
				host.setDesc(params.get("desc"));
				flash.success(Messages.get("SaveHostSuccess"));
				edithost(params.get("name"));
				}
				
			}		
		}
		
		// Must be a new host - send it to add service
		String name = params.get("name");
		validation.required(name);
		validation.match(name,VALIDNAMEREGEX);
		if (validation.hasErrors()) {
			// Used to identify the period that had the error
			validation.keep();
			addhost();
		}
		
		XMLHost host = new XMLHost();
		host.setName(params.get("name"));
		host.setDesc(params.get("desc"));
		config.getHost().add(host);
		flash.success(Messages.get("SaveHostSuccess"));
		addservice(params.get("name"));
	}

	
	/*************************************
	 * 
	 * Service section
	 * 
	 *************************************/
	
	
	/**
	 * Add a new service to an existing host.
	 * @param hostname
	 */
	public static void addservice(String hostname) {
		XMLService service = new XMLService();
		service.setName("");
		service.setDesc("");
		service.setDriver("");
		service.setUrl("");
		service.setSendserver(true);
		render(hostname,service);
	}

	/**
	 * Edit a service based on host and service name
	 * @param hostname
	 * @param servicename
	 */
	public static void editservice(String hostname, String servicename) {
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname)) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(servicename)) {
					
						if (service.isSendserver() == null){
							service.setSendserver(true);
						}
					
						render(host,service);
					}
				}
			}
		}
		render();
	}


	public static void deleteservice(String hostname, String servicename) {
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname)) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(servicename)) {
						host.getService().remove(service);
						flash.success(Messages.get("DeleteServiceSuccess"));
						edithost(hostname);
					}

				}
			}
		}
	}



	public static void saveservice() {
		
		XMLBischeck config = getCache();
		
		Iterator<XMLHost> hosts = config.getHost().iterator();
		// Check if an existing service - update
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(params.get("hostname"))) {
				Iterator<XMLService> services = host.getService().iterator();
				while(services.hasNext()){
					XMLService service = services.next();
					if (service.getName().equals(params.get("oldname"))) {
						
						String name = params.get("name");
						String  url = params.get("url");
						validation.required(name);
						validation.required(url);
						
						validation.match(name,VALIDNAMEREGEX);
						if (validation.hasErrors()) {
							// Used to identify the period that had the error
							validation.keep();
							editservice(params.get("hostname"),params.get("oldname"));
						}
						else {
							service.setName(params.get("name"));
							service.setDesc(params.get("desc"));
							service.setDriver(params.get("driver"));
							service.setUrl(params.get("url"));
							//if (!Bootstrap.getBischeckVersion().equals("0.3.3")) {
								if (params.get("sendserver").equalsIgnoreCase("true"))
									service.setSendserver(true);
								else
									service.setSendserver(false);
							//}
							flash.success(Messages.get("SaveServiceSuccess"));
							editservice(params.get("hostname"),params.get("name"));
						}
					}
				}
			}		
		}
		
		// Must be a new service - send it to add serviceitem
		String name = params.get("name");
		validation.required(name);
		validation.match(name,VALIDNAMEREGEX);
		if (validation.hasErrors()) {
			// Used to identify the period that had the error
			validation.keep();
			addservice(params.get("hostname"));
		}

		XMLService service = new XMLService();
		service.setName(params.get("name"));
		service.setDesc(params.get("desc"));
		service.setUrl(params.get("url"));
		service.setDriver(params.get("driver"));
		//if (!Bootstrap.getBischeckVersion().equals("0.3.3")) {
			service.setSendserver(Boolean.valueOf(params.get("sendserver")));
		//}
		service.getSchedule().add("5M");
		hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(params.get("hostname"))) {
				host.getService().add(service);
			}
		}
		flash.success(Messages.get("SaveServiceSuccess"));
		addserviceitem(params.get("hostname"),params.get("name"));
	}


	/*************************************
	 * 
	 * Service schedule section
	 * 
	 *************************************/
	
	public static void listserviceschedules(String hostname, String servicename){
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname)) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(servicename)) {
						render(host,service);
					}
				}
			}
		}
		render();
	}

	
	public static void addserviceschedule(String hostname, String servicename) {
		render(hostname,servicename);
		
	}

	
	public static void deleteserviceschedule(String hostname, String servicename, String schedule) {
		XMLBischeck config = getCache();
		
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(params.get("hostname"))) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(params.get("servicename"))) {
						List<String> schedulelist = service.getSchedule();						
						schedulelist.remove(params.get("schedule"));
						flash.success(Messages.get("DeleteScheduleSuccess"));
						listserviceschedules(params.get("hostname"), params.get("servicename"));
						
					}
				}
			}
		}
		render();
		
	}
	
	
	/**
	 * Edit the schedules for a service
	 * @param hostname
	 * @param servicename
	 */
	public static void editserviceschedule(String hostname, String servicename, String schedule) {
		if (CronExpression.isValidExpression(schedule)) {
			editCronserviceschedule(hostname, servicename, schedule);
		}
		else {
			editIntervalserviceschedule(hostname, servicename, schedule);
		}	
	}
	
	/**
	 * Edit the schedules for a service
	 * @param hostname
	 * @param servicename
	 */
	public static void editIntervalserviceschedule(String hostname, String servicename, String schedule) {
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname)) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(servicename)) {
						IntervalSchedule intervalschedule = null;
						if (schedule != null)
							intervalschedule = new IntervalSchedule(schedule); 
							
						render(host,service,schedule, intervalschedule);
					}
				}
			}
		}
		render();	
	}
	
	/**
	 * Edit the schedules for a service
	 * @param hostname
	 * @param servicename
	 */
	public static void editCronserviceschedule(String hostname, String servicename, String schedule) {
		XMLBischeck config = getCache();
		
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname)) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(servicename)) {
						
						CronSchedule cronschedule = null;
						
						// If error is set in the save 
						if (validation.hasErrors()) {
							cronschedule = new CronSchedule(); 
							
							if (flash.get("seconds") != null)
								cronschedule.seconds = flash.get("seconds"); 
							if (flash.get("minutes") != null)
								cronschedule.minutes = flash.get("minutes"); 
							if (flash.get("hours") != null)
								cronschedule.hours = flash.get("hours"); 
							if (flash.get("dayofmonth") != null)
								cronschedule.dayofmonth = flash.get("dayofmonth"); 
							if (flash.get("month") != null)
								cronschedule.month = flash.get("month"); 
							if (flash.get("dayofweek") != null)
								cronschedule.dayofweek = flash.get("dayofweek"); 
							if (flash.get("year") != null)
								cronschedule.year = flash.get("year"); 
						
						} else if (schedule != null)
							cronschedule = new CronSchedule(schedule); 
					
						render(host,service,schedule,cronschedule);
					}
				}
			}
		}
		render();	
	}


	public static void saveIntervalServiceSchedule() {
		
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(params.get("hostname"))) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(params.get("servicename"))) {
						List<String> schedulelist = service.getSchedule();
			
						String interval = params.get("interval");
						validation.required(interval);
						validation.match(interval,"[0-9]+");
						
						if (validation.hasErrors()) {
							// Used to identify the period that had the error
							validation.keep();
							editIntervalserviceschedule(params.get("hostname"), params.get("servicename"), params.get("curschedule"));
						}
			
						schedulelist.remove(params.get("curschedule"));
						schedulelist.add(params.get("interval")+params.get("resolution"));
						
						flash.success(Messages.get("SaveScheduleSuccess"));
						listserviceschedules(params.get("hostname"), params.get("servicename"));
						
					}
				}
			}
		}
		render();
	}

	public static void saveCronServiceSchedule() {
		
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(params.get("hostname"))) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(params.get("servicename"))) {
						List<String> schedulelist = service.getSchedule();
						
						
						if(validation.required(params.get("seconds")).error != null) 
							validation.addError("cronseconds","validation.cronsrequiered");
						
						if(validation.required(params.get("minutes")).error != null) 
							validation.addError("cronminutes","validation.cronsrequiered");
						
						if(validation.required(params.get("hours")).error != null) 
							validation.addError("cronhours","validation.cronsrequiered");
				
						if(validation.required(params.get("dayofmonth")).error != null) 
							validation.addError("crondayofmonth","validation.cronsrequiered");
												
						if(validation.required(params.get("month")).error != null) 
							validation.addError("cronmonth","validation.cronsrequiered");
						
						if(validation.required(params.get("dayofweek")).error != null) 
							validation.addError("crondayofweek","validation.cronsrequiered");
						
						
						Error error = validation.match(params.get("seconds"),
								"(((([0-9]|[0-5][0-9]),)*([0-9]|[0-5][0-9]))|(([\\*]|[0-9]|[0-5][0-9])(/|-)([0-9]|[0-5][0-9]))|([\\?])|([\\*]))").error;
						if(error != null) 
							validation.addError("cronseconds","validation.cronseconds");
						
						error = validation.match(params.get("minutes"),
								"(((([0-9]|[0-5][0-9]),)*([0-9]|[0-5][0-9]))|(([\\*]|[0-9]|[0-5][0-9])(/|-)([0-9]|[0-5][0-9]))|([\\?])|([\\*]))").error;
						if(error != null) 
							validation.addError("cronminutes","validation.cronminutes");
						
						error = validation.match(params.get("hours"),
								"(((([0-9]|[0-1][0-9]|[2][0-3]),)*([0-9]|[0-1][0-9]|[2][0-3]))|(([\\*]|[0-9]|[0-1][0-9]|[2][0-3])(/|-)([0-9]|[0-1][0-9]|[2][0-3]))|([\\?])|([\\*]))").error;
						if(error != null) 
							validation.addError("cronhours","validation.cronhours");
						
						error = validation.match(params.get("dayofmonth"),
								"(((([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1]),)*([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(C)?)|(([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(/|-)([1-9]|[0][1-9]|[1-2][0-9]|[3][0-1])(C)?)|(L(-[0-9])?)|(L(-[1-2][0-9])?)|(L(-[3][0-1])?)|(LW)|([1-9]W)|([1-3][0-9]W)|([\\?])|([\\*]))").error;
						if(error != null) 
							validation.addError("crondayofmonth","validation.crondayofmonth");
						
						error = validation.match(params.get("month"),
								"(((([1-9]|0[1-9]|1[0-2]),)*([1-9]|0[1-9]|1[0-2]))|(([1-9]|0[1-9]|1[0-2])(/|-)([1-9]|0[1-9]|1[0-2]))|(((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC),)*(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))|((JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(-|/)(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))|([\\?])|([\\*]))").error;
						if(error != null) 
							validation.addError("cronmonth","validation.cronmonth");
						
						error = validation.match(params.get("dayofweek"),
								"((([1-7],)*([1-7]))|([1-7](/|-)([1-7]))|(((MON|TUE|WED|THU|FRI|SAT|SUN),)*(MON|TUE|WED|THU|FRI|SAT|SUN)(C)?)|((MON|TUE|WED|THU|FRI|SAT|SUN)(-|/)(MON|TUE|WED|THU|FRI|SAT|SUN)(C)?)|(([1-7]|(MON|TUE|WED|THU|FRI|SAT|SUN))?(L|LW)?)|(([1-7]|MON|TUE|WED|THU|FRI|SAT|SUN)#([1-7])?)|([\\?])|([\\*]))").error;
						if(error != null) 
							validation.addError("crondayofweek","validation.crondayofweek");
						
						
						error = validation.match(params.get("year"),
								"(([\\*])?|(19[7-9][0-9])|(20[0-9][0-9]))?| (((19[7-9][0-9])|(20[0-9][0-9]))(-|/)((19[7-9][0-9])|(20[0-9][0-9])))?| ((((19[7-9][0-9])|(20[0-9][0-9])),)*((19[7-9][0-9])|(20[0-9][0-9])))").error;
						if(error != null) 
							validation.addError("cronyear","validation.cronyear");
						
						if (params.get("dayofweek").equals("*") && 
								params.get("dayofmonth").equals("*")) {
							validation.addError("crondayofweek", "validation.cronstar");
							validation.addError("crondayofmonth", "validation.cronstar");
						}
						
						if (params.get("dayofweek").equals("?") && 
								params.get("dayofmonth").equals("?")) {
							validation.addError("crondayofweek", "validation.cronquestion");
							validation.addError("crondayofmonth", "validation.cronquestion");
						}
						
						if (validation.hasErrors()) {
							// Used to identify the period that had the error
							validation.keep();
							params.flash();
							editCronserviceschedule(params.get("hostname"), params.get("servicename"), params.get("curschedule"));
						}
						
						schedulelist.remove(params.get("curschedule"));
						schedulelist.add(
								params.get("seconds") + " " +
								params.get("minutes") + " " +
								params.get("hours") + " " +
								params.get("dayofmonth") + " " +
								params.get("month") + " " +
								params.get("dayofweek") + " " +
								params.get("year") );

						flash.success(Messages.get("SaveScheduleSuccess"));
						listserviceschedules(params.get("hostname"), params.get("servicename"));

					}
				}
			}
		}
		render();
	}
	
	/*************************************
	 * 
	 * Service item section
	 * 
	 *************************************/
	
	public static void addserviceitem(String hostname, String servicename){
		
		XMLServiceitem serviceitem = new XMLServiceitem();
		serviceitem.setName("");
		serviceitem.setDesc("");
		serviceitem.setExecstatement("");
		serviceitem.setServiceitemclass("");
		serviceitem.setThresholdclass("");
		List<String> serviceitemclasses = Populate.getServiceItemClasses();
		List<String> thresholdclasses = Populate.getThresholdClasses();
		render(hostname,servicename,serviceitem,serviceitemclasses,thresholdclasses);
	}
	
	
	public static void saveserviceitem() {
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(params.get("hostname"))) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(params.get("servicename"))) {
						
						// Check if the serviceitem name already exists
						Iterator<XMLServiceitem> serviceitems;
						checkifserviceitemexists(service);
						
						serviceitems = service.getServiceitem().iterator();
						while (serviceitems.hasNext()){
							XMLServiceitem serviceitem = serviceitems.next();
							if (serviceitem.getName().equals(params.get("oldname"))) {
								String name = params.get("name");
								validation.required(name);
								validation.match(name,VALIDNAMEREGEX);
								if (validation.hasErrors()) {
									// Used to identify the period that had the error
									validation.keep();
									editserviceitem(params.get("hostname"),params.get("servicename"),params.get("oldname"));
								}
								else {
									serviceitem.setName(params.get("name"));
									serviceitem.setDesc(params.get("desc"));
									serviceitem.setExecstatement(params.get("execstatement"));
									serviceitem.setServiceitemclass(params.get("serviceitemclass"));
									serviceitem.setThresholdclass(params.get("thresholdclass"));
									flash.success(Messages.get("SaveServiceItemSuccess"));
									editserviceitem(params.get("hostname"),params.get("servicename"),params.get("name"));
								}
							}
						}
					}
				}
			}
		}
		// Must be a new serviceitem
		String name = params.get("name");
		validation.required(name);
		validation.match(name,VALIDNAMEREGEX);
		if (validation.hasErrors()) {
			// Used to identify the period that had the error
			validation.keep();
			addserviceitem(params.get("hostname"), params.get("servicename"));
		}
		
		XMLServiceitem serviceitem = new XMLServiceitem();
		serviceitem.setName(params.get("name"));
		serviceitem.setDesc(params.get("desc"));
		serviceitem.setExecstatement(params.get("execstatement"));
		serviceitem.setServiceitemclass(params.get("serviceitemclass"));
		serviceitem.setThresholdclass(params.get("thresholdclass"));
		
		hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			
			if (host.getName().equals(params.get("hostname"))) {
				Iterator<XMLService> services = host.getService().iterator();
				
				while (services.hasNext()) {
					XMLService service = services.next();
					
					if (service.getName().equals(params.get("servicename"))) {
						service.getServiceitem().add(serviceitem);
						flash.success(Messages.get("SaveServiceItemSuccess"));
						editservice(params.get("hostname"), params.get("servicename"));
					}
				}
			}
		}	
		render();
	}


	private static void checkifserviceitemexists(XMLService service) {
		Iterator<XMLServiceitem> serviceitems = service.getServiceitem().iterator();
		while (serviceitems.hasNext()){
		
			XMLServiceitem serviceitem = serviceitems.next();
			if (serviceitem.getName().equals(params.get("name")) && 
					!params.get("name").equals(params.get("oldname"))) {
		
				serviceitemexists(params.get("hostname"), 
						params.get("servicename"), 
						params.get("oldname"),
						params.get("name"));
			}
		}
	}
	
	
	public static void serviceitemexists(String hostname, String servicename, String oldserviceitemname, String newserviceitemname) {
		render(hostname, servicename, oldserviceitemname, newserviceitemname);
	}
	
	
	public static void editserviceitem(String hostname, String servicename, String serviceitemname){
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname)) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(servicename)) {
						Iterator<XMLServiceitem> serviceitems = service.getServiceitem().iterator();
						while (serviceitems.hasNext()){
							XMLServiceitem serviceitem = serviceitems.next();
							if (serviceitem.getName().equals(serviceitemname)) {
								List<String> serviceitemclasses = Populate.getServiceItemClasses();
								List<String> thresholdclasses = Populate.getThresholdClasses();
								render(host,service,serviceitem,serviceitemclasses,thresholdclasses);
							}
						}
					}
				}
			}
		}
		render();
	}
	
	
	public static void deleteserviceitem(String hostname, String servicename, String serviceitemname){
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname)) {
				Iterator<XMLService> services = host.getService().iterator();
				
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(servicename)) {
						Iterator<XMLServiceitem> serviceitems = service.getServiceitem().iterator();
	
						while (serviceitems.hasNext()){
							XMLServiceitem serviceitem = serviceitems.next();
							if (serviceitem.getName().equals(serviceitemname)) {
								service.getServiceitem().remove(serviceitem);
								flash.success(Messages.get("DeleteServiceItemSuccess"));
								editservice(hostname,servicename);
							}
		
						}
					}
				}
			}
		}
	}
	
}