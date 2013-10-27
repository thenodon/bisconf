package models;

import java.io.File;

public class ConfigMeta implements Comparable<ConfigMeta>{
	public String username;
	public String created;
	public String comment;
	public String directory;
	public boolean current = false;
	
	/*
	 * Showing newest version first.
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(ConfigMeta o) {
		if (Long.parseLong(this.directory) < Long.parseLong(o.directory)) return 1;
		if (Long.parseLong(this.directory) > Long.parseLong(o.directory)) return -1;
		
		return 0;
	}
	

}
