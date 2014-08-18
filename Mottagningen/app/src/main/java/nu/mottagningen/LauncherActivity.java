package nu.mottagningen;

import nu.mottagningen.contact.ContactActivity;
import nu.mottagningen.maps.MapActivity;
import nu.mottagningen.news.NewsCardActivity;
import nu.mottagningen.schedule.ScheduleActivity;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

/**
 * This is the MainActivity, it will start with the app and initiate preferences the first time it launches.
 * @author Andreas
 *
 */
public class LauncherActivity extends Activity {

    private int mapsClicks;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_launcher);
		PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        mapsClicks = 0;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.action_settings, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.action_settings:
			Intent intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_about:
	    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
	    	alertDialogBuilder
	    		.setTitle(R.string.title_about)
	    		.setMessage(R.string.about_text)
	    		.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {		//Create and show am About-dialog.
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
	    	AlertDialog dialog = alertDialogBuilder.create();
	    	dialog.show();
	    	return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	/**
	 * When the "News"-button is clicked, start the NewsCardActivity
	 */
	public void newsButtonClicked(View v) {
//		String newsURL = getApplicationContext().getResources().getString(R.string.rss_feed_uri);
		String newsURL = PreferenceManager.getDefaultSharedPreferences(this).getString(getResources().getString(R.string.pref_news_source), null);
		Intent intent = new Intent(this, NewsCardActivity.class);
		intent.putExtra(NewsCardActivity.EXTRA_URL, newsURL);
		startActivity(intent);
	}
	
	/**
	 * When the Map-button is clicked, start the MapActivity
	 */
	public void mapButtonClicked(View v) {
//		Intent intent = new Intent(this, MapActivity.class);
//		startActivity(intent);
        switch (mapsClicks) {
            case 0:
                Toast.makeText(this, "Feature removed in 2014", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            case 1:
                Toast.makeText(this, "Still not here", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            case 2:
                Toast.makeText(this, "What are you doing?", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            case 3:
                Toast.makeText(this, "Just stop..", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            case 4:
                Toast.makeText(this, "This is pointless, and stopped being fun exactly 3 clicks ago.", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            case 5:
                Toast.makeText(this, "Dude or Dudette, stop!", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            case 6:
                Toast.makeText(this, "Pleeeeease stop!! :(", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            case 7:
                Toast.makeText(this, "What's that light over there?", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            case 8:
                Toast.makeText(this, "No way...", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            case 9:
                Toast.makeText(this, "Must...Not...Walk...Towards...", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            case 10:
                Toast.makeText(this, "...Light...", Toast.LENGTH_SHORT).show();
                mapsClicks++;
                break;
            default:
                mapsClicks = 0;
                Toast.makeText(this, "Wtf? I was sure I was going to crash.. Anyway, enjoy the complete void of functionality in this feature.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(this, MapActivity.class);
        	    startActivity(intent);
        }
	}
	
	/**
	 * When the Schedule-button is clicked, start the ScheduleActivity
	 */
	public void scheduleButtonClicked(View v) {
		Intent intent = new Intent(this, ScheduleActivity.class);
		startActivity(intent);
	}
	
	/**
	 * When the Contact-button is clicked, start the ContactActivity
	 */
	public void contactButtonClicked(View v) {
		Intent intent = new Intent(this, ContactActivity.class);
		startActivity(intent);
	}
}
