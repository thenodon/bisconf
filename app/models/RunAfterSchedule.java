package models;

import play.db.jpa.Model;
import play.i18n.Messages;



public class RunAfterSchedule extends Model {

	public String hostname;
	public String servicename;
	

	public RunAfterSchedule(String hostname, String servicename) {
    	this.hostname = hostname;
    	this.servicename = servicename;
	}

	public RunAfterSchedule(String schedule) {
		int index = schedule.indexOf("-");
    	   	
    	this.hostname = schedule.substring(0, index);
    	this.servicename = schedule.substring(index+1, schedule.length());

	}

}


