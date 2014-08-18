package nu.mottagningen.schedule;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import edu.emory.mathcs.backport.java.util.Collections;

import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.Component;
import net.fortuna.ical4j.model.ComponentList;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.Property;
import nu.mottagningen.DatabaseHandler;
import nu.mottagningen.R;

import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.ProgressDialog;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ExpandableListView;
import android.widget.Toast;

/**
 * ScheduleActivity takes data from a URL pointing to an iCal-file, and displays it in a calendar/schedule/agenda.
 *TODO: If the calendar spans more than a year, the calendar will not display correctly!
 * @author Andreas
 *
 */
public class ScheduleActivity extends Activity {
	
	private ExpandableListView expandableList;	//The view that will contain a list of Schedule-entries.
	private ExpandableListAdapter adapter;		//Adapter used to populate the listview.
	private List<String> dateGroups;			//A list of start dates, will be used to display different groups of dates (eg. 12 Jan 2013).
	private boolean listExpanded = false;		//Used to toggle expansion of the list
	private Menu menu;
	private DatabaseHandler db;
	
	private ProgressDialog progressDialog;		//Displays the progress when updating schedule.
	
	private static final String STATE_LIST_EXPANDED = "nu.mottagningen.state_list_expanded";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_schedule);
		
		expandableList = (ExpandableListView) findViewById(R.id.date_list);
		dateGroups = new ArrayList<String>();
		
		db = new DatabaseHandler(this);
		Map<String, List<ScheduleEntry>> map = db.getAllCalendarEntries();		
		dateGroups.addAll(map.keySet());
		Collections.sort(dateGroups, ScheduleEntry.keyComparator());
		adapter = new ExpandableListAdapter(this, dateGroups, map);
		expandableList.setAdapter(adapter);
		
		expandToday();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.schedule_menu, menu);
		this.menu = menu;
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.expand_schedule:
			toggleExpandSchedule();
			break;
		case R.id.update_schedule:
			new FetchCalendarTask().execute();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		if(progressDialog != null)
			progressDialog.dismiss();
		progressDialog = null;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		progressDialog = new ProgressDialog(this);
		progressDialog.setTitle(getResources().getString(R.string.progress_title_updating_schedule));
		progressDialog.setCanceledOnTouchOutside(false);
		progressDialog.setMessage(getResources().getString(R.string.progress_connecting));

		if(dateGroups.isEmpty())
			new FetchCalendarTask().execute();		//Try to update the Schedule.
	}
	
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBoolean(STATE_LIST_EXPANDED, listExpanded);
	}
	
	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		listExpanded = savedInstanceState.getBoolean(STATE_LIST_EXPANDED);
	}
	
	//Toggle expansion of all Schedule entries. TODO: Do some more checking before toggling, are all entries already expanded? Also make sure icon updates when the schedule does.2
	private void toggleExpandSchedule() {
		int groups = adapter.getGroupCount();
		MenuItem toggleExpand = menu.findItem(R.id.expand_schedule);
		if(listExpanded) {
			for(int i=0; i<groups; i++)
				expandableList.collapseGroup(i);
			toggleExpand.setIcon(android.R.drawable.arrow_down_float);
			toggleExpand.setTitle(R.string.schedule_expand_text);
		}
		else {
			for(int i=0; i<groups; i++)
				expandableList.expandGroup(i);
			toggleExpand.setIcon(android.R.drawable.arrow_up_float);
			toggleExpand.setTitle(R.string.schedule_collapse_text);
		}
		expandToday();
		listExpanded = !listExpanded;
	}
	
	/**
	 * Expand the list item representing today's agenda.
	 */
	private void expandToday() {
		Date now = new Date();
		String nowString = ScheduleEntry.DATE_FORMAT.format(now);
		Log.d("ScheduleActivity", "Now = " + nowString);
		
		for(int i=0; i<dateGroups.size(); i++) {
			if(dateGroups.get(i).equals(nowString)) {
				expandableList.expandGroup(i);
				expandableList.setSelectedGroup(i);
			}
		}
	}
	
	/**
	 * This task will connect to a given URL pointing to a .ics-file. It will download and parse it (using ical4j), and insert all entries into a database.
	 * If the database already contains a schedule (which could be from another source or out dated), it will be cleared. The GUI will be updated to display progress and update views after execution.
	 * @author Andreas
	 *
	 */
	private class FetchCalendarTask extends AsyncTask<Void, String, Boolean> {
		
		private Calendar calendar;
		private ArrayList<String> groups;
		private Map<String, List<ScheduleEntry>> entryMap;
		private String errorMessage = "";
		
		private String PROGRESS_CONNECTING;
		private String PROGRESS_DOWNLOADING;
		private String PROGRESS_FINISHING;
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			PROGRESS_CONNECTING = getResources().getString(R.string.progress_connecting);
			PROGRESS_DOWNLOADING = getResources().getString(R.string.progress_downloading);
			PROGRESS_FINISHING = getResources().getString(R.string.progress_finishing);
			if(progressDialog != null)
				progressDialog.show();
		}
		
		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				this.publishProgress(PROGRESS_CONNECTING);
				//TODO: Use intents to do this.
				String urlString = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getResources().getString(R.string.pref_calendar_source), null);
				URL url = new URL(urlString);
				InputStream in = url.openStream();
				CalendarBuilder builder = new CalendarBuilder();
				publishProgress(PROGRESS_DOWNLOADING);	//Connection complete, start downloading
				calendar = builder.build(in);				//Build a Calendar from the .ics-file.
			} catch (IOException e) {
				e.printStackTrace();
				errorMessage = getResources().getString(R.string.message_connection_error);
				return false;
			} catch (ParserException e) {
				e.printStackTrace();
				errorMessage = getResources().getString(R.string.message_parsing_error);
				return false;
			}
			publishProgress(PROGRESS_FINISHING);	//Download complete, start updating the local database.
			ComponentList list = calendar.getComponents();
			db.clearScheduleTable();
			List<ScheduleEntry> entryList = new ArrayList<ScheduleEntry>();
			for(int i=list.size()-1; i>=0; i--) {		//Go through the list in reverse, shouldn't need to do this anymore. TODO: reverse
				Component c = (Component) list.get(i);
				try {
					Property prop;
					String uid = null;
					String name = null;
					String description = null;
					DateTime start = null;
					DateTime end = null;
					String location = null;
					if((prop = c.getProperty(Property.UID)) != null)
						uid = prop.getValue();
					if((prop = c.getProperty(Property.SUMMARY)) != null)				//Do lots of null checks to avoid errors.
						name = prop.getValue();
					if((prop = c.getProperty(Property.DESCRIPTION)) != null)
						description = prop.getValue();
					if((prop = c.getProperty(Property.DTSTART)) != null)
						start = new DateTime(prop.getValue());
					if((prop = c.getProperty(Property.DTEND)) != null)
						end = new DateTime(prop.getValue());
					if((prop = c.getProperty(Property.LOCATION)) != null)
						location = prop.getValue();
					
					ScheduleEntry entry = new ScheduleEntry(start, end, name, description, location, uid);
//					db.insertScheduleEntry(entry);
					if(uid != null)
						entryList.add(entry);
				} catch (ParseException e) {
					e.printStackTrace();
//					errorMessage = getResources().getString(R.string.message_parsing_error);
//					return false;
				}
			}
			db.insertScheduleEntries(entryList);	//Insert all new entries
			
			entryMap = db.getAllCalendarEntries();	//Get all entries. TODO: If there are performance issues, this could probably do without all the writing and reading to the db.
			groups = new ArrayList<String>(entryMap.keySet());
			Collections.sort(groups, ScheduleEntry.keyComparator());
			return true;
		}
		
		@Override
		protected void onPostExecute(Boolean success) {
			super.onPostExecute(success);
			//If update was successful, display a confirmation message to the user and update the Expandable List (and Adapter).
			if(success) {
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_schedule_update_success), Toast.LENGTH_SHORT).show();
				dateGroups = groups;
				adapter = new ExpandableListAdapter(getApplicationContext(), dateGroups, entryMap);
				expandableList.setAdapter(adapter);
			} else {
				Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
			}
			if(progressDialog != null)
				progressDialog.dismiss();
			expandToday();
		}
		
		@Override
		protected void onProgressUpdate(String... progressState) {
			super.onProgressUpdate(progressState);
			if(progressDialog != null)
				progressDialog.setMessage(progressState[0]);
		}
		
	}
}
