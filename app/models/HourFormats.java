package models;

import java.util.List;

import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLHourinterval;
import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLHours;

public class HourFormats {
	public enum HourDef {
		HOUR24 { 
            public String toString() {
                return "HOUR24";
            }
         }, 
        HOURINTERVAL { 
            public String toString() {
                return "HOURINTERVAL";
            }   
        }
	}

	private XMLHours xmlhours;
	private HourDef hourdef;
	
	public HourFormats(XMLHours xmlhours) {
		this.xmlhours = xmlhours;
		if (this.xmlhours.getHourinterval().size() == 0) 
			hourdef = HourDef.HOUR24;
		else 
			hourdef = HourDef.HOURINTERVAL;
	}
	
	public HourDef getTypeOfHour() { 
		return hourdef;
	}
	
	public List<String> getHour24() {
		return xmlhours.getHour();
	}
	
	public List<XMLHourinterval> getHourInterval() {
		return xmlhours.getHourinterval();
	}
	
	public int getID() {
		return xmlhours.getHoursID();
	}
}
