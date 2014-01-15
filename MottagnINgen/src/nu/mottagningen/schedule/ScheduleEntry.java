package nu.mottagningen.schedule;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import net.fortuna.ical4j.model.DateTime;

/**
 * Wrapper class for Schedule entries. Each entry contains about the same information as a Google Calendar entry does (start, end, description, summary, location, uid).
 * Since ScheduleEntry is Comparable, it can be sorted and ordered by its' start date.
 * @author Andreas
 *
 */
public class ScheduleEntry implements Comparable<ScheduleEntry> {
	private DateTime start;			//The start date
	private DateTime end;			//The end date
	private String name;			//Name of the event, same as an ical Description.
	private String description;		//Description of the event, same as an ical Summary.
	private String location;		//Location of the event
	private boolean reminder;		//Determines whether the user wants to reminded before this event. TODO: Not implemented
	private String UID;				//A unique id for every event.
	
	public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("EEEEEEEEE, d MMM", Locale.UK);
	
	public ScheduleEntry(DateTime start, DateTime end, String name,
			String description, String location, String UID) {
		this.start = start;
		this.end = end;
		this.name = name;
		this.description = description;
		this.location = location;
		this.reminder = false;
		this.UID = UID;
	}
	
	public ScheduleEntry(DateTime start, DateTime end, String name,
			String description, String location, boolean reminder, String UID) {
		this.start = start;
		this.end = end;
		this.name = name;
		this.description = description;
		this.location = location;
		this.reminder = reminder;
		this.UID = UID;
	}
	
	public String getUID() {
		return UID;
	}
	
	public void setUID(String UID) {
		this.UID = UID;
	}
	
	public boolean getReminder() {
		return reminder;
	}
	
	public void setReminder(boolean reminder) {
		this.reminder = reminder;
	}

	public DateTime getStart() {
		return start;
	}

	public void setStart(DateTime start) {
		this.start = start;
	}

	public DateTime getEnd() {
		return end;
	}

	public void setEnd(DateTime end) {
		this.end = end;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}
	
	public String getKey() {
		return DATE_FORMAT.format(getStart());
	}

	@Override
	public int compareTo(ScheduleEntry other) {
		return getStart().compareTo(other.getStart());
	}
	
	/**
	 * Comparator used to compare different Keys (start dates)
	 * @return
	 */
	public static Comparator<String> keyComparator() {
		return new Comparator<String>() {
			@Override
			public int compare(String lhs, String rhs) {
				int result = 0;
				try {
					Date lhDate = DATE_FORMAT.parse(lhs);
					Date rhDate = DATE_FORMAT.parse(rhs);
					result = lhDate.compareTo(rhDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				return result;
			}
		};
	}
}
