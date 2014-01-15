package nu.mottagningen;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.fortuna.ical4j.model.DateTime;
import nu.mottagningen.schedule.ScheduleEntry;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * DatabaseHandler will help executing MySQL-queries on the application database..
 * @author Andreas
 *
 */
public class DatabaseHandler extends SQLiteOpenHelper {
	
//	private static String TAG = "DatabaseHandler";
	
	private static final int DATABASE_VERSION = 6;
	private static final String DATABASE_NAME = "mottagningen_db";
	
	private static final String TABLE_SCHEDULE = "schedule";
	private static final String KEY_UID = "uid";					//Cursor index 0
	private static final String KEY_NAME = "name";					//Cursor index 1
	private static final String KEY_DESCRIPTION = "description";	//Cursor index 2
	private static final String KEY_START = "start";				//Cursor index 3
	private static final String KEY_END = "end";					//Cursor index 4
	private static final String KEY_LOCATION = "location";			//Cursor index 5
	private static final String KEY_REMINDER = "reminder";			//Cursor index 6

	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_CALENDAR_TABLE_QUERY = "CREATE TABLE " + TABLE_SCHEDULE + "("
				+ KEY_UID + " TEXT PRIMARY KEY NOT NULL,"
				+ KEY_NAME + " TEXT,"
				+ KEY_DESCRIPTION + " TEXT,"
				+ KEY_START + " TEXT,"
				+ KEY_END + " TEXT,"
				+ KEY_LOCATION + " TEXT,"
				+ KEY_REMINDER + " INTEGER)";
		try {
			db.execSQL(CREATE_CALENDAR_TABLE_QUERY);
			Log.d("DATABASE", "Created table " + TABLE_SCHEDULE);
		} catch (SQLiteException e) {
			Log.e("DATABASE", e.toString());
		}
	}
	
//	@Override
//	public void onConfigure(SQLiteDatabase db) {
//		super.onConfigure(db);					
//		db.enableWriteAheadLogging();			//TODO: If concurrency problems occur, this may solve it. Though it may decrease performance.
//	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_SCHEDULE);
		onCreate(db);
	}
	
	/**
	 * Completely clears the Calendar-table.
	 */
	public synchronized void clearScheduleTable() {
		SQLiteDatabase db = this.getWritableDatabase();
		db.delete(TABLE_SCHEDULE, null, null);
		db.close();
	}
	
	/**
	 * Adds a ScheduleEntry to the database, if there is an entry with the same UID, it will be replaced with the new one. 
	 * @param entry
	 */
	public synchronized void insertScheduleEntry(ScheduleEntry entry) {
		int reminder = entry.getReminder() ? 1 : 0;
		
		SQLiteDatabase db = this.getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put(KEY_UID, entry.getUID());
		values.put(KEY_NAME, entry.getName());
		values.put(KEY_DESCRIPTION, entry.getDescription());
		values.put(KEY_START, entry.getStart().toString());
		values.put(KEY_END, entry.getEnd().toString());
		values.put(KEY_LOCATION, entry.getLocation());
		values.put(KEY_REMINDER, reminder);
		db.insertWithOnConflict(TABLE_SCHEDULE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		db.close();
	}
	
	/**
	 * Inserts all entries in the given list. Older entries with the same UID will be replaced.
	 * @param entries
	 */
	public synchronized void insertScheduleEntries(List<ScheduleEntry> entries) {
		SQLiteDatabase db = this.getWritableDatabase();
		for(ScheduleEntry entry : entries) {
			int reminder = entry.getReminder() ? 1 : 0;
			ContentValues values = new ContentValues();
			values.put(KEY_UID, entry.getUID());
			values.put(KEY_NAME, entry.getName());
			values.put(KEY_DESCRIPTION, entry.getDescription());
			values.put(KEY_START, entry.getStart().toString());
			values.put(KEY_END, entry.getEnd().toString());
			values.put(KEY_LOCATION, entry.getLocation());
			values.put(KEY_REMINDER, reminder);
			db.insertWithOnConflict(TABLE_SCHEDULE, null, values, SQLiteDatabase.CONFLICT_REPLACE);
		}
		db.close();
	}
	
	/**
	 * 
	 * @return A LinkedHashMap with all calendar entries in the database.
	 */
	public synchronized Map<String, List<ScheduleEntry>> getAllCalendarEntries() {
		Map<String, List<ScheduleEntry>> map = new LinkedHashMap<String, List<ScheduleEntry>>();
//		SQLiteDatabase db = this.getReadableDatabase();
		SQLiteDatabase db = this.getWritableDatabase();
		String selectQuery = "SELECT * FROM " + TABLE_SCHEDULE;
		Cursor cursor = db.rawQuery(selectQuery, null);
		
		if(cursor.moveToFirst()) {
			do {
				boolean reminder = (cursor.getInt(6) == 1);
				ScheduleEntry entry;
				try {
					entry = new ScheduleEntry(
							new DateTime(cursor.getString(3)), 
							new DateTime(cursor.getString(4)), 
							cursor.getString(1), 
							cursor.getString(2), 
							cursor.getString(5),
							reminder,
							cursor.getString(0));
					String key = entry.getKey();
					if(map.get(key) == null)
						map.put(key, new ArrayList<ScheduleEntry>());
					map.get(key).add(entry);
				} catch (ParseException e) {
					e.printStackTrace();
				}
			} while(cursor.moveToNext());
		}
		cursor.close();
		db.close();
		
		for(String key : map.keySet())
			Collections.sort(map.get(key));
		
		return map;
	}
}
