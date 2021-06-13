package structure;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

public class Photo implements Comparable<Photo>, Serializable{
	private static final long serialVersionUID = 1L;
	
	public String caption;
	public String path;
	public Date date;
	public ArrayList<String> tags;
	
	public Photo(String path) {
		File fp = new File(path);
		this.path = path;
		date = new Date(fp.lastModified());
		caption = "untitled " + date.toString();
		tags = new ArrayList<String>();
	}
	
	public String toString() {
		return caption;
	}
	
	@Override
	public int compareTo(Photo o) {
		return this.path.compareTo(o.path);
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Photo)) return false;
		else return this.path.equals(((Photo) o).path);
	}
}
