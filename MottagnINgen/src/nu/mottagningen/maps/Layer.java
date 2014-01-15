package nu.mottagningen.maps;

import java.util.ArrayList;

/**
 * A Layer is basically just an ArrayList, but it has a name that can be used to identify it (and display it). The list contain different map overlays(?) like points of interest, lines, polygons etc.
 * @author Andreas
 *
 */
public class Layer extends ArrayList<Placemark>{
	/**
	 * 
	 */
	private static final long serialVersionUID = -4225538532241784321L;
	private String name;
	
	public Layer(String name) {
		super();
		this.name = name;
	}
	
	public Layer() {
		super();
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	@Override
	public String toString() {
		return name;
	}
}
