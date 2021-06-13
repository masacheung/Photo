package structure;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javafx.collections.ObservableList;

public class Album implements Serializable{
	private static final long serialVersionUID = 1L;
	
	public String name;
	public Date firstDate;
	public Date lastDate;
	public ArrayList<Photo> photos;
	
	public Album(String name) {
		this.name = name;
		this.photos = new ArrayList<Photo>();
	}
	
	public Album(String name, ObservableList<Photo> lst) {
		this.name = name;
		this.photos = new ArrayList<Photo>();
		
		for (Photo p : lst) {
			photos.add(p);
		}
	}
	
	public void add(Photo p) {
		photos.add(p);
		if (firstDate == null) {
			firstDate = p.date;
			lastDate = p.date;
		}
		else if (firstDate.after(p.date))
			firstDate = p.date;
		else if (lastDate.before(p.date))
			lastDate = p.date;
	}
	
	public void remove(Photo p) {
		photos.remove(p);
		// Update Dates
		if (photos.isEmpty()) {
			firstDate = null;
			lastDate = null;
		} else {
			Photo temp = photos.get(0);		// Get first photo
			if (p.date.equals(firstDate))
				firstDate = temp.date;
			if (p.date.equals(lastDate))
				lastDate = temp.date;
			// Update appropriately
			for (Photo pic: photos) {
				if (pic.date.before(firstDate))
					firstDate = pic.date;
				else if (pic.date.after(lastDate))
					lastDate = pic.date;
			}
		}
		
	}
	
	public String toString() {
		return this.name;
	}
}
