package nu.mottagningen.maps;

import java.util.ArrayList;

/**
 * A Placemark contains about the same information as a Placemark-tag in a .kml-file. This can be a marker, a polygon, or a polyline.
 * @author Andreas
 *
 */
public class Placemark {
	
	private String name;			//The name/title of this placemark.
	private String description;		//A longer description of what this placemark represents.
	private ArrayList<String> coordinates;	//The coordinates in this placemark, if it is a Marker, this should only contain one set of coordinates.
	private int type;			//The type of this placemark (Marker, Polygon, Polyline etc.)
	
	
	public static final int TYPE_MARKER = 11;
	public static final int TYPE_POLYGON = 22;
	public static final int TYPE_POLYLINE = 33;
	
	public Placemark(String name, String description, String coordinates, int type) {
		this.name = name;
		this.description = description;
		this.coordinates = parseCoordinates(coordinates);
		this.type = type;
	}
	
	public Placemark() {
		
	}
	
	/**
	 * Since the coordinates is just a string with sets of coordinates (separated by " "), this method is used to parse that string and make a list out of it. 
	 * @param coordinates
	 * @return
	 */
	private ArrayList<String> parseCoordinates(String coordinates) {
		ArrayList<String> list = new ArrayList<String>();
		String[] coords = coordinates.split(" ");
		for(String s : coords)
			list.add(s);
		return list;
	}

	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public ArrayList<String> getCoordinates() {
		return coordinates;
	}
	
	public int getType() {
		return type;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCoordinates(ArrayList<String> coordinates) {
		this.coordinates = coordinates;
	}
	
	public void setCoordinates(String coordinates) {
		this.coordinates = parseCoordinates(coordinates);
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
}