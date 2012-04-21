package models;

import play.db.jpa.Model;
import play.i18n.Messages;



public class IntervalSchedule extends Model {

	public String interval;
	public String resolution;
	public String label;


	public IntervalSchedule(String schedule) {
		if (schedule.length()!=0) { 
			String withoutSpace=schedule.replaceAll(" ","");
			char time = withoutSpace.charAt(withoutSpace.length()-1);
			resolution=""+time;
			interval = withoutSpace.substring(0, withoutSpace.length()-1);
			switch (time) {
			case 'S': label = Messages.get("Seconds");
			break;
			case 'M': label = Messages.get("Minutes");
			break;
			case 'H': label = Messages.get("Hours");
			break;
			}
		}
		else {
			resolution = "S";
			label = Messages.get("Seconds");
		}

	}

}


