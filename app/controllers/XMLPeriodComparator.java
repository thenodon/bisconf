package controllers;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.ingby.socbox.bischeck.xsd.twenty4threshold.XMLPeriod;
/**
 * The class provide a sorting for the map of values of XMLPeriods where 
 * the key is the hashcode of the XMLPeriod object. This is used to manage 
 * a virtual id of the period in the context of a servicedef.
 * The sorting order is the hour id where the lowest id is 
 * first in the list.
 * @author andersh
 *
 */
public class XMLPeriodComparator implements Comparator {
/*
	Map<Integer, XMLPeriod> base;
	public XMLPeriodComparator(Map base) {
		this.base = base;
	}

	public int compare(Object a, Object b) {

		if(((XMLPeriod) base.get(a)).getHoursIDREF() < 
				((XMLPeriod) base.get(b)).getHoursIDREF()) {
			return -1;
		} else if(((XMLPeriod) base.get(a)).getHoursIDREF() == 
			((XMLPeriod) base.get(b)).getHoursIDREF()) {
			
			return 0;
		} else {
			return 1;
		}
	}
*/
	List<XMLPeriod> base;
	public XMLPeriodComparator(List base) {
		this.base = base;
	}

	public int compare1(Object a, Object b) {

		if( ((XMLPeriod) a).getHoursIDREF() < ((XMLPeriod) b).getHoursIDREF()) {
			return -1;
		} else if(((XMLPeriod) a).getHoursIDREF() == ((XMLPeriod) b).getHoursIDREF()) {
			return 0;
		} else {
			return 1;
		}
	}

	public int compare(Object a, Object b) {
		boolean amonth = ((XMLPeriod) a).getMonths().isEmpty();
		System.out.println("amonth " + amonth);
		boolean bmonth = ((XMLPeriod) b).getMonths().isEmpty();
		System.out.println("bmonth " + bmonth);
		boolean aweek = ((XMLPeriod) a).getWeeks().isEmpty();
		System.out.println("aweek " + aweek);
		boolean bweek = ((XMLPeriod) b).getWeeks().isEmpty();
		System.out.println("bweek " + bweek);
		
		if (amonth && aweek) 
			return 1;
		
		if (amonth && !bmonth)
			return 1;
		
		if (aweek && !bweek)
			return 1;

		if (bmonth && bweek) 
			return -1;
		
		if (bmonth && !amonth)
			return -1;
		
		if (bweek && !aweek)
			return 1;

		return 0;
		/*
		if( ((XMLPeriod) a).getHoursIDREF() < ((XMLPeriod) b).getHoursIDREF()) {
			return -1;
		} else if(((XMLPeriod) a).getHoursIDREF() == ((XMLPeriod) b).getHoursIDREF()) {
			return 0;
		} else {
			return 1;
		}*/
	}

}