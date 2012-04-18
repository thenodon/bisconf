package controllers;

import play.*;
import play.cache.Cache;
import play.mvc.*;

import java.util.*;

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
				host.setName(params.get("name"));
				host.setDesc(params.get("desc"));
				edithost(params.get("name"));
			}		
		}
		
		// Must be a new host - send it to add service
		XMLHost host = new XMLHost();
		host.setName(params.get("name"));
		host.setDesc(params.get("desc"));
		config.getHost().add(host);
		addservice(params.get("name"));
	}

	
	/*************************************
	 * 
	 * Service section
	 * 
	 *************************************/
	
	/**
	 * 
	 * @param hostname
	 */
	/*
	public static void listservices(String hostname){
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname))
				render(host,host.getService());
		}
		render();
	}
*/
	
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
						//render(host,service,serviceitem);
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
						service.setName(params.get("name"));
						service.setDesc(params.get("desc"));
						service.setDriver(params.get("driver"));
						service.setUrl(params.get("url"));
						if (params.get("sendserver").equalsIgnoreCase("true"))
							service.setSendserver(true);
						else
							service.setSendserver(false);
						
						editservice(params.get("hostname"),params.get("name"));
					}
				}
			}		
		}
		
		// Must be a new service - send it to add serviceitem
		XMLService service = new XMLService();
		service.setName(params.get("name"));
		service.setDesc(params.get("desc"));
		service.setUrl(params.get("url"));
		service.setDriver(params.get("driver"));
		service.setSendserver(Boolean.valueOf(params.get("sendserver")));
		service.getSchedule().add("5M");
		hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(params.get("hostname"))) {
				host.getService().add(service);
			}
		}
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
		XMLBischeck config = getCache();
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			if (host.getName().equals(hostname)) {
				Iterator<XMLService> services = host.getService().iterator();
				while (services.hasNext()) {
					XMLService service = services.next();
					if (service.getName().equals(servicename)) {
						render(host,service,schedule);
					}
				}
			}
		}
		render();	
	}
	
	
	public static void saveserviceschedule() {
			
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
						
						if (params.get("newschedule") == null) {
							schedulelist.remove(params.get("curschedule"));
							schedulelist.add(params.get("schedule"));
						}
						else {
							schedulelist.add(params.get("newschedule"));
						}
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
		render(hostname,servicename,serviceitem);
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
								serviceitem.setName(params.get("name"));
								serviceitem.setDesc(params.get("desc"));
								serviceitem.setExecstatement(params.get("execstatement"));
								serviceitem.setServiceitemclass(params.get("serviceitemclass"));
								serviceitem.setThresholdclass(params.get("thresholdclass"));
								editserviceitem(params.get("hostname"),params.get("servicename"),params.get("name"));
								
							}
						}
					}
				}
			}
		}
		// Must be a new serviceitem
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
								render(host,service,serviceitem);
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
								editservice(hostname,servicename);
							}
		
						}
					}
				}
			}
		}
	}
	
}