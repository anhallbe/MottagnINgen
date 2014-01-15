package nu.mottagningen.maps;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * A parser used to parse KML-files from google mapsengine.
 * @author Andreas
 *
 */
public class KMLParser {
	
	/**
	 * Parses an InputStream from a KML-file.
	 * @param is - Input stream from a file (network, local, etc)
	 * @return A list of "layers"
	 */
	public static List<Layer> parse(InputStream is) {
		List<Layer> layers = null;
		XMLReader reader;
		try {
			reader = SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			KMLHandler handler = new KMLHandler();
			reader.setContentHandler(handler);
			reader.parse(new InputSource(is));
			layers = handler.getValues();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return layers;
	}
}
