package nu.mottagningen.maps;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;

import nu.mottagningen.R;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient.ConnectionCallbacks;
import com.google.android.gms.common.GooglePlayServicesClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * MapActivity displays a map, it can display custom layers fetched from Google Mapsengine.
 * @author Andreas
 *
 */
public class MapActivity extends FragmentActivity implements ConnectionCallbacks, OnConnectionFailedListener, LocationListener, OnItemSelectedListener {
	
	private GoogleMap gmap;
	private LocationClient locationClient;
	private Spinner spinner;
	private ArrayList<Layer> layers = new ArrayList<Layer>();
	ArrayAdapter<Layer> adapter;
	
	public final static String EXTRA_STARTLATITUDE = "nu.mottagningen.EXTRA_STARTLATITUDE";
	public final static String EXTRA_STARTLONGITUDE = "nu.mottagningen.EXTRA_STARTLONGITUDE";
	
	  private static final LocationRequest REQUEST = LocationRequest.create()
		      .setInterval(5000)         // 5 seconds
		      .setFastestInterval(16)    // 16ms = 60fps
		      .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_map);
		setUpMapIfNeeded();
		
		//Initiate the Layer-spinner.
		spinner = (Spinner) findViewById(R.id.spinner);
		spinner.setOnItemSelectedListener(this);
		spinner.setPrompt("Layers");
		adapter = new ArrayAdapter<Layer>(this, android.R.layout.simple_spinner_dropdown_item, layers);
		spinner.setAdapter(adapter);
		
		new FetchKMLTask().execute();
		
		Intent intent = getIntent();
		double startLat =  intent.getDoubleExtra(EXTRA_STARTLATITUDE, -1);
		double startLng = intent.getDoubleExtra(EXTRA_STARTLONGITUDE, -1);
		if(startLat != -1 && startLng != -1) {
			Log.d("MapActivity", "Moving camera to " + startLat + ", " + startLng);
			gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(startLat, startLng), 17));
		}
	}
	
	/**
	 * Initializes the map if it hasn't been done yet, otherwise it does nothing.
	 */
	private void setUpMapIfNeeded() {
		if(gmap == null) {
			gmap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
			if(gmap != null) {
				gmap.setMyLocationEnabled(true);
			}
		}
	}
	
	/**
	 * Initializes the location client if it hasn't been done yet, otherwise it does nothing.
	 */
	private void setUpLocationClientIfNeeded() {
		if(locationClient == null)
			locationClient = new LocationClient(getApplicationContext(), this, this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		setUpMapIfNeeded();
		setUpLocationClientIfNeeded();
		locationClient.connect();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(locationClient != null)
			locationClient.disconnect();
	}

	@Override
	public void onConnected(Bundle arg0) {
		locationClient.requestLocationUpdates(REQUEST, this);
	}
	
	@Override
	public void onLocationChanged(Location loc) {
	}
	
	@Override
	public void onConnectionFailed(ConnectionResult arg0) {
	}

	@Override
	public void onDisconnected() {
	}

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Layer layer = (Layer) parent.getItemAtPosition(position);
		paintLayer(layer);
		if(!layer.isEmpty()) {
			String[] coords = layer.get(0).getCoordinates().get(0).split(",");							//When a Layer is clicked (in the spinner),
			LatLng latlng = new LatLng(Double.parseDouble(coords[1]), Double.parseDouble(coords[0]));	//display the overlays in that Layer,
			gmap.animateCamera(CameraUpdateFactory.newLatLngZoom(latlng, 15));							//and move the camera to the first Placemark in the Layer.
		}
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		
	}
	
	/**
	 * Clear the map of all overlays, and display the given Layer.
	 * @param layer
	 */
	private void paintLayer(Layer layer) {
		setUpMapIfNeeded();
		gmap.clear();
		
		//Go through the list of Placemarks in the Layer, perform appropriate operations to display the different types of Placemarks.
		for(Placemark p : layer) {
			String name = p.getName();
			String description = p.getDescription();
			ArrayList<String> coordinates = p.getCoordinates();
			
			if(p.getType() == Placemark.TYPE_MARKER) {
				String[] coords = coordinates.get(0).split(",");
				double latitude = Double.parseDouble(coords[1]);
				double longitude = Double.parseDouble(coords[0]);
				gmap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude)).title(name).snippet(description).visible(true));
			}
			else if(p.getType() == Placemark.TYPE_POLYLINE) {							//Add line
				PolylineOptions options = new PolylineOptions();
				for(String c : coordinates) {
					String[] coords = c.split(",");
					double latitude = Double.parseDouble(coords[1]);
					double longitude = Double.parseDouble(coords[0]);
					options.add(new LatLng(latitude, longitude));
					Log.d("MapTestActivity", "Adding polyline: " + name + ". Coords: " + latitude + ", " + longitude);
				}
				options.color(Color.RED);
				options.width(5);
				options.visible(true);
				gmap.addPolyline(options);
			}
			else if(p.getType() == Placemark.TYPE_POLYGON) {
				PolygonOptions options = new PolygonOptions();
				for(String c : coordinates) {
					String[] coords = c.split(",");
					double latitude = Double.parseDouble(coords[1]);
					double longitude = Double.parseDouble(coords[0]);
					options.add(new LatLng(latitude, longitude));
					Log.d("MapTestActivity", "Adding polygon: " + name + ". Coords: " + latitude + ", " + longitude);
				}
				options.strokeColor(Color.argb(255, 0, 0xFFFF, 0));
				options.fillColor(Color.argb(125, 0, 0xFFFF, 0));
				options.visible(true);
				gmap.addPolygon(options);
			}
		}
	}
	
	/**
	 * Downloads a .kml-file from a given URL (preferably google maps engine). The file is parsed into java-friendly objects (Layers, Placemarks). After it has been downloaded and parsed, the Map will display the information in a user-friendly way.
	 * @author Andreas
	 *
	 */
	private class FetchKMLTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				String urlString = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getResources().getString(R.string.pref_map_source), null); //TODO Use Intent instead...
				URL url = new URL(urlString);
				InputStream is = url.openStream();
				layers = (ArrayList<Layer>) KMLParser.parse(is);
				is.close();
				for(Layer l : layers) {
					Log.d("MapTestActivity", "Layer name = " + l.getName());
					for(Placemark p : l)
						Log.d("MapTestActivity", "Placemark = " + p.getName());
				}
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			if(success) {
				adapter.clear();
				adapter.add(new Layer("None"));
				for(Layer l : layers)
					adapter.add(l);
				adapter.notifyDataSetChanged();
			} else
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_connection_error), Toast.LENGTH_SHORT).show();
		}
	}
}
