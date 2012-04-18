package controllers;

import play.*;
import play.cache.Cache;
import play.libs.XML;
import play.mvc.*;

import java.util.*;

import com.ingby.socbox.bischeck.ConfigFileManager;
import com.ingby.socbox.bischeck.ConfigXMLInf;
import com.ingby.socbox.bischeck.ConfigurationManager;
import com.ingby.socbox.bischeck.ConfigXMLInf.XMLCONFIG;
import com.ingby.socbox.bischeck.serviceitem.ServiceItem;
import com.ingby.socbox.bischeck.threshold.Twenty4HourThreshold;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLBischeck;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLHost;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLService;
import com.ingby.socbox.bischeck.xsd.bischeck.XMLServiceitem;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperties;
import com.ingby.socbox.bischeck.xsd.properties.XMLProperty;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLHoliday;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLHours;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLMonths;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLPeriod;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLServicedef;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLTwenty4Threshold;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLWeeks;

import models.*;

@With(Secure.class)
public class Twenty4Threshold extends BasicController {
	
	
	public static XMLTwenty4Threshold getCache(){
		XMLTwenty4Threshold config = 
			(XMLTwenty4Threshold) SessionData.getXMLConfig(ConfigXMLInf.XMLCONFIG.TWENTY4HOURTHRESHOLD,
					XMLTwenty4Threshold.class);
		return config;
	}
	
	
	public static void index() {
		render();
	}

	
	/*
	 * Check if the serviceitems threshold class is Twenty4HourThreshold
	 */
	private static Boolean isTwenty4ThresholdClass(XMLServiceitem serviceitem) {
		
		if (serviceitem.getThresholdclass() == null) 
			return false;
		
		if (Twenty4HourThreshold.class.getName().contains(serviceitem.getThresholdclass())) 
			return true;
		
		return false;
	}

	
	/**
	 * Create of all Host, Service, ServiceItems where ServiceItems has
	 * threshold class Twenty4HourThreshold
	 * @return 
	 */
	private static List<ServiceDefs> prepare(){
		
		List<ServiceDefs> servicedeflist = new ArrayList<ServiceDefs>();
		
		XMLBischeck config = Bischeck.getCache();
		
		Iterator<XMLHost> hosts = config.getHost().iterator();
		while (hosts.hasNext()){
			XMLHost host = hosts.next();
			Iterator<XMLService> services = host.getService().iterator();
			while (services.hasNext()) {
				XMLService service = services.next();
				Iterator<XMLServiceitem> serviceitems;
				serviceitems = service.getServiceitem().iterator();
				while (serviceitems.hasNext()){
					XMLServiceitem serviceitem = serviceitems.next();
					ServiceDefs servicedef = new ServiceDefs();
					if (isTwenty4ThresholdClass(serviceitem)) {
						servicedef.hostname = host.getName();
						servicedef.servicename = service.getName();
						servicedef.serviceitemname = serviceitem.getName();
						servicedeflist.add(servicedef);
					}
				}
			}
		}
		return servicedeflist;	
	}

	
	/**
	 * Retrieve all configured hoursIDs
	 * @return
	 */
	private static List<Integer> getAllConfiguredHourID() {
		XMLTwenty4Threshold config = Twenty4Threshold.getCache();
		
		Iterator<XMLHours> hours = config.getHours().iterator();
	
		List<Integer> allhours = new ArrayList<Integer>();
	
		while (hours.hasNext()){
			XMLHours hour = hours.next();
			allhours.add(hour.getHoursID());
		}
		
		Collections.sort(allhours);
		return allhours;
	}

	
	/**
	 * List all SerivceDefs but only the one that has a Twenty4HourThreshold class
	 */
	public static void listServiceDefs()  {
		List<ServiceDefs> servicedefs = prepare();
		render(servicedefs);
	}
		
	
	/**
	 * Edit 
	 * @param hostname
	 * @param servicename
	 * @param serviceitemname
	 */
	public static void editServiceDefs(String hostname, String servicename, String serviceitemname) {
		
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLServicedef> servicedefs = config.getServicedef().iterator();
		
		while (servicedefs.hasNext()){
			XMLServicedef servicedef = servicedefs.next();
			
			/*
			 * Get 
			 */
			if(servicedef.getHostname().equals(hostname) && 
				servicedef.getServicename().equals(servicename) &&
				servicedef.getServiceitemname().equals(serviceitemname) ) {
		
				List<XMLPeriod> periodlist = servicedef.getPeriod();
				
				// Get all hour def existing for the periods
				Map<Integer, List<String>> hours = getHoursByPeriod(periodlist);
				
				Map<Integer, XMLPeriod> hashperiod = createIndexPeriodMap(periodlist);
				
				List<Integer> allhourids = getAllConfiguredHourID();
 				
				render(servicedef,hashperiod,hours, allhourids);
			}
		}
		
		/*
		 * If a new
		 */
		XMLServicedef servicedef = new XMLServicedef();
		servicedef.setHostname(hostname);
		servicedef.setServicename(servicename);
		servicedef.setServiceitemname(serviceitemname);
		
		render(servicedef);
	}

	
	private static Map<Integer,List<String>> getHoursByPeriod(List<XMLPeriod> periods) {
		
		XMLTwenty4Threshold config = getCache();
			
		Map<Integer,List<String>> hours = new HashMap<Integer, List<String>>();
		
		Iterator<XMLPeriod> perioditer = periods.iterator();
		while (perioditer.hasNext()) {
			XMLPeriod period = perioditer.next();
			Iterator<XMLHours> hoursiter = config.getHours().iterator();
	
			while(hoursiter.hasNext()) {
				XMLHours hour = hoursiter.next();
				if (hour.getHoursID() == period.getHoursIDREF()) {
					hours.put(period.getHoursIDREF(),hour.getHour());
				}
			}
		}
 		return hours;
	}
	
	
	private static XMLPeriod getPeriodByIndex(XMLServicedef servicedef, Integer index) {

		List<XMLPeriod> periodlist = servicedef.getPeriod();
		Map<Integer, XMLPeriod> hashperiod = createIndexPeriodMap(periodlist);

		return hashperiod.get(index);
	}
	
	
	/**
	 * Create a Map of all periods based on a servicedef list of XMLPeriod. 
	 * The key is the hashcode of the XMLPeriod object
	 * @param periodlist
	 * @return
	 */
	private static Map<Integer, XMLPeriod> createIndexPeriodMap(
			List<XMLPeriod> periodlist) {
		Iterator<XMLPeriod> perioditer = periodlist.iterator();

		Map<Integer,XMLPeriod> hashperiod = new HashMap<Integer, XMLPeriod>();
		int count =0;
		while(perioditer.hasNext()) {
			count++;
			XMLPeriod period = perioditer.next();
			hashperiod.put(period.hashCode(), period);
		}
		
		// Sort the list of XMLPeriods based on the XMLPeriodComparator 
		XMLPeriodComparator bvc =  new XMLPeriodComparator(hashperiod);
        TreeMap<Integer,XMLPeriod> sorted_map = new TreeMap(bvc);
        sorted_map.putAll(hashperiod);
			
		return sorted_map;
	}
	
	
	public static void deleteServiceDefs(String hostname, String servicename, String serviceitemname, int period) {
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLServicedef> servicedefs = config.getServicedef().iterator();
		
		while (servicedefs.hasNext()){
			XMLServicedef servicedef = servicedefs.next();
			
			/*
			 * Get 
			 */
			if(servicedef.getHostname().equals(hostname) && 
				servicedef.getServicename().equals(servicename) &&
				servicedef.getServiceitemname().equals(serviceitemname) ) {
		
				List<XMLPeriod> periodlist = servicedef.getPeriod();
								
				Map<Integer, XMLPeriod> hashperiod = createIndexPeriodMap(periodlist);
				
				XMLPeriod delperiod = hashperiod.get(period);
				
				periodlist.remove(delperiod);
				
				break;
			}
		}
		
		editServiceDefs(hostname, servicename, serviceitemname);
	}
	
	
	public static void addServiceDefs(String hostname, String servicename, String serviceitemname){
		XMLTwenty4Threshold config = getCache();
		
		Iterator<XMLServicedef> servicedefs = config.getServicedef().iterator();
		
		while (servicedefs.hasNext()){
			XMLServicedef servicedef = servicedefs.next();
			
			/*
			 * If exisiting and only additional XMLPeriod 
			 */
			if(servicedef.getHostname().equals(hostname) && 
				servicedef.getServicename().equals(servicename) &&
				servicedef.getServiceitemname().equals(serviceitemname) ) {
		
				servicedef.getPeriod().add(new XMLPeriod());
				editServiceDefs(hostname, servicename, serviceitemname);
			}
		}
		
		
		// if new and not existing
		
		XMLServicedef servicedef = new XMLServicedef();
		servicedef.setHostname(hostname);
		servicedef.setServicename(servicename);
		servicedef.setServiceitemname(serviceitemname);
		servicedef.getPeriod().add(new XMLPeriod());
		
		config.getServicedef().add(servicedef);
		editServiceDefs(hostname, servicename, serviceitemname);
	}

	
	public static void selectHourID() {
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLServicedef> servicedefs = config.getServicedef().iterator();
	
		while (servicedefs.hasNext()){
			XMLServicedef servicedef = servicedefs.next();
			if(servicedef.getHostname().equals(params.get("hostname")) && 
				servicedef.getServicename().equals(params.get("servicename")) &&
				servicedef.getServiceitemname().equals(params.get("serviceitemname")) ) {
				
				XMLPeriod period = getPeriodByIndex(servicedef, Integer.parseInt(params.get("periodindex")));
				
				period.setHoursIDREF(Integer.parseInt(params.get("hourid")));
				
			}
		}
		editServiceDefs(params.get("hostname"), params.get("servicename"), params.get("serviceitemname"));
	}
	
	
	/**
	 * Save the period
	 */
	public static void savePeriodData() {
		
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLServicedef> servicedefs = config.getServicedef().iterator();
	
		while (servicedefs.hasNext()){
			XMLServicedef servicedef = servicedefs.next();
	
			if(servicedef.getHostname().equals(params.get("hostname")) && 
				servicedef.getServicename().equals(params.get("servicename")) &&
				servicedef.getServiceitemname().equals(params.get("serviceitemname")) ) {
				XMLPeriod period = getPeriodByIndex(servicedef, Integer.parseInt(params.get("periodindex")));
				period.setCalcmethod(params.get("calcmethod"));
				period.setWarning(Integer.parseInt(params.get("warning")));
				period.setCritical(Integer.parseInt(params.get("critical")));
			}
		}
		
		editServiceDefs(params.get("hostname"), params.get("servicename"), params.get("serviceitemname"));
	}
	
	
	public static void deletePeriodMonth(String hostname, 
			String servicename, 
			String serviceitemname, 
			Integer periodindex, 
			Integer month,
			Integer dayofmonth) {
		
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLServicedef> servicedefs = config.getServicedef().iterator();
	
		while (servicedefs.hasNext()){
			XMLServicedef servicedef = servicedefs.next();
			
			if(servicedef.getHostname().equals(params.get("hostname")) && 
				servicedef.getServicename().equals(params.get("servicename")) &&
				servicedef.getServiceitemname().equals(params.get("serviceitemname")) ) {
	
				XMLPeriod period = getPeriodByIndex(servicedef, Integer.parseInt(params.get("periodindex")));
				
				Iterator<XMLMonths> monthiter = period.getMonths().iterator();
				boolean found2delete = false;
				XMLMonths month2del = null;
				while (monthiter.hasNext()) {
					month2del = monthiter.next();
					if (month2del.getMonth() == month && month2del.getDayofmonth() == dayofmonth) {
						found2delete = true;
						break;
					}
				}
				if(found2delete)
					period.getMonths().remove(month2del);
			}
		}
	
		editServiceDefs(params.get("hostname"), params.get("servicename"), params.get("serviceitemname"));
	}
	
	
	public static void addPeriodMonth() {
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLServicedef> servicedefs = config.getServicedef().iterator();
	
		while (servicedefs.hasNext()){
			XMLServicedef servicedef = servicedefs.next();
	
			if(servicedef.getHostname().equals(params.get("hostname")) && 
				servicedef.getServicename().equals(params.get("servicename")) &&
				servicedef.getServiceitemname().equals(params.get("serviceitemname")) ) {
	
				XMLPeriod period = getPeriodByIndex(servicedef, Integer.parseInt(params.get("periodindex")));
			
				XMLMonths month2add = new XMLMonths();
				if (!params.get("month").equalsIgnoreCase("None"))
					month2add.setMonth(Integer.parseInt(params.get("month")));
				if (!params.get("dayofmonth").equalsIgnoreCase("None"))
					month2add.setDayofmonth(Integer.parseInt(params.get("dayofmonth")));
				
				
				period.getMonths().add(month2add);
			}
		}
	
		editServiceDefs(params.get("hostname"), params.get("servicename"), params.get("serviceitemname"));
	}
	
	
	public static void deletePeriodWeek(String hostname, 
			String servicename, 
			String serviceitemname, 
			Integer periodindex, 
			Integer week,
			Integer dayofweek) {
		
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLServicedef> servicedefs = config.getServicedef().iterator();
	
		while (servicedefs.hasNext()){
			XMLServicedef servicedef = servicedefs.next();
	
			if(servicedef.getHostname().equals(params.get("hostname")) && 
				servicedef.getServicename().equals(params.get("servicename")) &&
				servicedef.getServiceitemname().equals(params.get("serviceitemname")) ) {
	
				XMLPeriod period = getPeriodByIndex(servicedef, Integer.parseInt(params.get("periodindex")));
				
				Iterator<XMLWeeks> weekiter = period.getWeeks().iterator();
				boolean found2delete = false;
				XMLWeeks week2del = null;
				while (weekiter.hasNext()) {
					week2del = weekiter.next();
					if (week2del.getWeek() == week && week2del.getDayofweek() == dayofweek) {
						found2delete = true;
						break;
					}
				}
				if(found2delete)
					period.getWeeks().remove(week2del);
			}
		}	
		editServiceDefs(params.get("hostname"), params.get("servicename"), params.get("serviceitemname"));
	}
	
	
	public static void addPeriodWeek() {
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLServicedef> servicedefs = config.getServicedef().iterator();
	
		while (servicedefs.hasNext()){
			XMLServicedef servicedef = servicedefs.next();
	
			if(servicedef.getHostname().equals(params.get("hostname")) && 
				servicedef.getServicename().equals(params.get("servicename")) &&
				servicedef.getServiceitemname().equals(params.get("serviceitemname")) ) {
	
				XMLPeriod period = getPeriodByIndex(servicedef, Integer.parseInt(params.get("periodindex")));
				XMLWeeks week2add = new XMLWeeks();
			
				if (!params.get("week").equalsIgnoreCase("None"))
					week2add.setWeek(Integer.parseInt(params.get("week")));
				
				if (!params.get("dayofweek").equalsIgnoreCase("None"))
					week2add.setDayofweek(Integer.parseInt(params.get("dayofweek")));
				
				
				period.getWeeks().add(week2add);
			}
		}
	
		editServiceDefs(params.get("hostname"), params.get("servicename"), params.get("serviceitemname"));
	
	}
	
	
	/*
	 * Holidays 	
	 */
	public static void listHolidays() {
		XMLTwenty4Threshold config = getCache();
		List<XMLHoliday> holidays = config.getHoliday();	
		render(holidays);
	}
	
	
	public static void listYear(String year) {
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLHoliday> holidays = config.getHoliday().iterator();	
		
		while(holidays.hasNext()) {
			XMLHoliday holiday = holidays.next();
			
			if (holiday.getYear() == Integer.parseInt(year)) {
				Collections.sort(holiday.getDayofyear());
				render(holiday);
			}
		}
		index();
	}
	
	
	public static void deleteYear(String year) {	
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLHoliday> holidays = config.getHoliday().iterator();
		boolean removefound = false;
		XMLHoliday holiday = null;
		while (holidays.hasNext()) {
			holiday = holidays.next();
			if (Integer.parseInt(year) == holiday.getYear()) {
				removefound=true;
				break;
			}
		}	
		if (removefound)
			config.getHoliday().remove(holiday);
		listHolidays();
	}
	
	
	public static void addYear(){
		render();
	}
	
	
	public static void saveYear() {
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLHoliday> holidays = config.getHoliday().iterator();	
	
		while (holidays.hasNext()) {
			XMLHoliday holiday = holidays.next();
			if (Integer.parseInt(params.get("year")) == holiday.getYear()) {
				listHolidays();
			}
		}	
		
		XMLHoliday holiday = new XMLHoliday();
		holiday.setYear(Integer.parseInt(params.get("year")));
		config.getHoliday().add(holiday);
		listHolidays();
	}
	
	
	public static void editDay(String year,String day){
		render(day,year);
	}
	
	
	public static void deleteDay(String year,String day){
		XMLTwenty4Threshold config = getCache();
		
		Iterator<XMLHoliday> holidays = config.getHoliday().iterator();
		// Check if an existing host - update
		while (holidays.hasNext()){
			XMLHoliday holiday = holidays.next();
			if (Integer.toString(holiday.getYear()).equals(params.get("year"))) {
				
				List<String> days = holiday.getDayofyear();
				days.remove(params.get("day"));
			}		
		}
		listYear(params.get("year"));
	}

	
	public static void saveDay(){	
		/*
		 * Check if already exist
		 * Check format - One string
		 */
		XMLTwenty4Threshold config = getCache();
		
		Iterator<XMLHoliday> holidays = config.getHoliday().iterator();
		// Check if an existing host - update
		while (holidays.hasNext()){
			XMLHoliday holiday = holidays.next();
			if (Integer.toString(holiday.getYear()).equals(params.get("year"))) {
				if (params.get("oldday") != null) {
					holiday.getDayofyear().remove(params.get("oldday"));
					holiday.getDayofyear().add(params.get("day"));
				} else {
					holiday.getDayofyear().add(params.get("day"));
				}
				listYear(params.get("year"));
			}		
		}
	}

	
	public static void addDay(Integer year){
		render(year);
	}
	
	

	/*
	 * Hours 	
	 */

	private static Map<Integer,Set<XMLServicedef>> getHourRealtions() {
		XMLTwenty4Threshold config = getCache();
		
		Map<Integer,Set<XMLServicedef>>  def2hour = new TreeMap<Integer,Set<XMLServicedef>>();
		
		// init the mapping
		Iterator<XMLHours> hoursiter = config.getHours().iterator();
		while (hoursiter.hasNext()) {
			XMLHours hour = hoursiter.next();
			def2hour.put(hour.getHoursID(), new HashSet<XMLServicedef>());
		}
		
		hoursiter = config.getHours().iterator();
		while (hoursiter.hasNext()) {
			XMLHours hour = hoursiter.next();
			hour.getHoursID();
			Iterator<XMLServicedef> servicedefs = config.getServicedef().iterator();

			while (servicedefs.hasNext()) {
				XMLServicedef servicedef = servicedefs.next();
				Iterator<XMLPeriod> periods = servicedef.getPeriod().iterator();
			
				while(periods.hasNext()) {
					XMLPeriod period = periods.next();
				
					if (hour.getHoursID() == period.getHoursIDREF()) {
						def2hour.get(hour.getHoursID()).add(servicedef);
					}
				}
			}
		}
		
		return def2hour;
	}
	
	
	public static void listHours() {
				
		Map<Integer,Set<XMLServicedef>>  hour2def = getHourRealtions();
		
		render(hour2def);
	}
	
	
	public static void addHour() {
		List<Integer> hourids = getAllConfiguredHourID();
	
		int hourid = getNextValue(hourids);
		editHour(hourid);
	}

	
	@SuppressWarnings("unused")
	private static Integer getMaxValue(List<Integer> numbers){  
		int maxValue = 0;  
		Iterator<Integer> iter = numbers.iterator();
		while (iter.hasNext()){
			Integer val = iter.next();  
			if(maxValue < val){  
				maxValue = val;  
			}  
		}  
		return new Integer(maxValue);  
	}  
	
	
	private static Integer getNextValue(List<Integer> numbers){  
		Collections.sort(numbers);
		int nextValue = 0;  
		Iterator<Integer> iter = numbers.iterator();
	
		while (iter.hasNext()){
			Integer val = iter.next();  
			if((nextValue+1) == val || nextValue == val){  
				nextValue = val;  
			}  
			else 
				break;
		}  
		
		return new Integer(nextValue+1);  
	}
	
	
	public static void saveHour() {
		XMLTwenty4Threshold config = getCache();
		Iterator<XMLHours> hours = config.getHours().iterator();
		
		// If an update
		while (hours.hasNext()) {
			XMLHours hour24 = hours.next();

			if (hour24.getHoursID() == Integer.parseInt(params.get("hourid"))) {
				for(int i=0;i<24;i++) {
					hour24.getHour().remove(i);
					if (i<10){
						hour24.getHour().add(i, params.get("0"+i));
					}
					else {
						hour24.getHour().add(i, params.get(""+i));
					}
				}
				editHour(Integer.parseInt(params.get("hourid")));

			}
		}

		// If hour id is new 

		List<XMLHours> hourlist = config.getHours();
		XMLHours hour24 = new XMLHours();
		hour24.setHoursID(Integer.parseInt(params.get("hourid")));
		
		for(int i=0;i<24;i++) {
			
			if (i<10){
				hour24.getHour().add(i, params.get("0"+i));
			}
			else {
				hour24.getHour().add(i, params.get(""+i));
			}
		}
		
		hourlist.add(hour24);
		editHour(Integer.parseInt(params.get("hourid")));
	}

	
	public static void editHour(int id) {
		XMLTwenty4Threshold config = getCache();
		
		Map<Integer,Set<XMLServicedef>> hourrel = getHourRealtions();
		List<XMLServicedef> hour2def = null;
		if (hourrel.get(id) != null) {
			hour2def = new ArrayList<XMLServicedef>(hourrel.get(id)); 
		}
	
		Iterator<XMLHours> hours = config.getHours().iterator();
		while (hours.hasNext()) {
			XMLHours hour24 = hours.next();
			
			if (hour24.getHoursID() == id) {
				render(hour24,hour2def);
			}
		}
		
		// Do not exists
		XMLHours hour24 = new XMLHours();
		hour24.setHoursID(id);
		List<String> hourelements = hour24.getHour();
		
		for (int i =0; i<24;i++) {
			hourelements.add(i,"null");
		}
		
		render(hour24,hour2def);
	}

	
	public static void deleteHour(int id) {
		XMLTwenty4Threshold config = getCache();
		
		Map<Integer,Set<XMLServicedef>> hourrel = getHourRealtions();
		
		Iterator<XMLHours> hours = config.getHours().iterator();
	
		while (hours.hasNext()) {
			XMLHours hour24 = hours.next();

			if (hour24.getHoursID() == id && hourrel.get(id).size() == 0) {
				config.getHours().remove(hour24);
				break;
			}
		}
		
		listHours();
	}
}


