package models;

import java.util.StringTokenizer;

import org.quartz.CronExpression;

import play.db.jpa.Model;
import play.i18n.Messages;



public class CronSchedule extends Model {

	public String seconds;
	public String minutes;
	public String hours;
	public String dayofmonth;
	public String month;
	public String dayofweek;
	public String year;


	public CronSchedule() {
	}
	
	public CronSchedule(String schedule) {
		if (CronExpression.isValidExpression(schedule)) {
		//if (schedule.length() != 0) {
			String[] strarr = schedule.split(" ");

			seconds = strarr[0];
			minutes = strarr[1];
			hours= strarr[2];
			dayofmonth= strarr[3];
			month= strarr[4];
			dayofweek= strarr[5];
			if (strarr.length == 7)
				year = strarr[6];
		//}
		}
	}

}




