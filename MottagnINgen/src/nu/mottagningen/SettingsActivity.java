package nu.mottagningen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;

public class SettingsActivity extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	
	private EditTextPreference calendarSource;
	private EditTextPreference mapSource;
	private ListPreference newsSource;
	
	private String KEY_NEWS_SOURCE;
	private String KEY_CALENDAR_SOURCE;
	private String KEY_MAP_SOURCE;
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
		setContentView(R.layout.activity_settings);
		
		KEY_NEWS_SOURCE = getResources().getString(R.string.pref_news_source);
		KEY_CALENDAR_SOURCE = getResources().getString(R.string.pref_calendar_source);
		KEY_MAP_SOURCE = getResources().getString(R.string.pref_map_source);
		
		newsSource = (ListPreference) findPreference(KEY_NEWS_SOURCE);
		calendarSource = (EditTextPreference) findPreference(KEY_CALENDAR_SOURCE);
		mapSource = (EditTextPreference) findPreference(KEY_MAP_SOURCE);
		
		setSummary(newsSource);
		setSummary(calendarSource);
		setSummary(mapSource);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
	}
	
	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}
	
	/**
	 * Sets the summary of the given Preference, provided it is a EditTextPreference or ListPreference.
	 * @param pref
	 */
	private void setSummary(Preference pref) {
		if(pref instanceof EditTextPreference)
			pref.setSummary(((EditTextPreference) pref).getText());
		else if(pref instanceof ListPreference)
			pref.setSummary(((ListPreference) pref).getEntry());
	}

	@SuppressWarnings("deprecation")
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Log.d("SettingsActivity", "Setting changed. Key=" + key);
		setSummary(findPreference(key));
	}
	
	/**
	 * When the "Reset"-button is clicked, reset all preference and also restart the activity to update the views.
	 */
	public void resetPreferencesToDefault(View v) {
		PreferenceManager.getDefaultSharedPreferences(this).edit().clear().commit();
		PreferenceManager.setDefaultValues(this, R.xml.preferences, true);
		
		Intent intent = getIntent();							//
		overridePendingTransition(0, 0);						//
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);		//TODO Fix temporary solution when reseting preferences.
		finish();												//Temporary solution to reset the Views displaying the preferences.
		overridePendingTransition(0, 0);						//This will restart the Activity without animations.
		startActivity(intent);									//
	}
}