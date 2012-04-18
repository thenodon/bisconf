package models;

import java.io.File;

public class ConfigMeta implements Comparable<ConfigMeta>{
	public String username;
	public String created;
	public String comment;
	public String directory;
	public boolean current = false;
	
	
	@Override
	public int compareTo(ConfigMeta o) {
		if (Long.parseLong(this.directory) < Long.parseLong(o.directory)) return -1;
		if (Long.parseLong(this.directory) > Long.parseLong(o.directory)) return 1;
		
		return 0;
	}
/*	@Override
	public int compareTo(ConfigMeta o) {
		if (Long.parseLong(this.directory.substring(this.directory.lastIndexOf(File.separator)+1) ) < 
				Long.parseLong(o.directory.substring(o.directory.lastIndexOf(File.separator)+1))) return -1;
		if (Long.parseLong(this.directory.substring(this.directory.lastIndexOf(File.separator)+1) ) > 
				Long.parseLong(o.directory.substring(o.directory.lastIndexOf(File.separator)+1))) return 1;
		
		return 0;
	}
*/
}
