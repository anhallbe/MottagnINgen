package nu.mottagningen.maps;

import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * A helper-class to handle KML-files (used by SAXParserFactory).
 * @author Andreas
 *
 */
public class KMLHandler extends DefaultHandler {
	
	private List<Layer> layers;
	private Layer currentLayer;
	private Placemark currentPlacemark;
	private StringBuilder builder;
	
	public KMLHandler() {
		layers = new ArrayList<Layer>();
	}

	public List<Layer> getValues() {
		return layers;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		builder = new StringBuilder();
		if(qName.equalsIgnoreCase("Placemark"))
			currentPlacemark = new Placemark();
		else if(qName.equalsIgnoreCase("Folder"))
			currentLayer = new Layer();
	}
	
	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		if(builder != null) {
			for(int i=start; i<(start+length); i++)
				builder.append(ch[i]);
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		if(qName.equalsIgnoreCase("Placemark")) {
			currentLayer.add(currentPlacemark);
			currentPlacemark = null;
		}
		else if(qName.equalsIgnoreCase("Folder")) {
			layers.add(currentLayer);
			currentLayer = null;
		}
		else if(qName.equalsIgnoreCase("name") && currentLayer != null && currentPlacemark == null)
			currentLayer.setName(builder.toString());
		else if(qName.equalsIgnoreCase("name") && currentPlacemark != null)
			currentPlacemark.setName(builder.toString());
		else if(qName.equalsIgnoreCase("description") && currentPlacemark != null)
			currentPlacemark.setDescription(builder.toString());
		else if(qName.equalsIgnoreCase("coordinates") && currentPlacemark != null)
			currentPlacemark.setCoordinates(builder.toString());
		else if(qName.equalsIgnoreCase("Point"))
			currentPlacemark.setType(Placemark.TYPE_MARKER);
		else if(qName.equalsIgnoreCase("LineString"))
			currentPlacemark.setType(Placemark.TYPE_POLYLINE);
		else if(qName.equalsIgnoreCase("LinearRing"))
			currentPlacemark.setType(Placemark.TYPE_POLYGON);
	}
}