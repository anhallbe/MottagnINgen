package nu.mottagningen.schedule;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import nu.mottagningen.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

/**
 * This Adapter is used to populate a list of Schedule-entries. Entries are grouped by their start date.
 * @author Andreas
 *
 */
public class ExpandableListAdapter extends BaseExpandableListAdapter{
	
	private Context context;
	private Map<String, List<ScheduleEntry>> entries;	//A map of Schedule-entries. Entries are grouped by their start dates (which is used as keys in this Map).
	private List<String> keys;	//A list of start dates, used as keys.
	
	public ExpandableListAdapter(Context context, List<String> keys, Map<String, List<ScheduleEntry>> entries) {
		this.context = context;
		this.entries = entries;
		this.keys = keys;
	}

	@Override
	public Object getChild(int groupPos, int childPos) {
		return entries.get(keys.get(groupPos)).get(childPos);
	}

	@Override
	public long getChildId(int groupPos, int childPos) {
		return childPos;
	}

	@Override
	public View getChildView(int groupPos, int childPos, boolean isLastChild, View convertView, ViewGroup parent) {
		final ScheduleEntry entry = (ScheduleEntry) getChild(groupPos, childPos);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		if(convertView == null)
			convertView = inflater.inflate(R.layout.schedule_entry_layout, null);
		
		TextView timeView = (TextView) convertView.findViewById(R.id.schedule_time_view);
		TextView descriptionView = (TextView) convertView.findViewById(R.id.schedule_description_view);
		TextView locationView = (TextView) convertView.findViewById(R.id.schedule_location_view);
		
		SimpleDateFormat format = new SimpleDateFormat("HH:mm", Locale.UK);	//TODO Using constant Locale.UK. This should probably be done differently.
		
		timeView.setText(format.format(entry.getStart()) + " - " + format.format(entry.getEnd()));		//00:00 - 11:11
		descriptionView.setText(entry.getName());
		locationView.setText(entry.getLocation());
		
		
		//When an entry is clicked, display a Dialog with a detailed description of the event.
		final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(parent.getRootView().getContext());
		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
		    	alertDialogBuilder
		    		.setTitle(entry.getName())
		    		.setMessage(entry.getDescription())
		    		.setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {		//Show some information about the event when clicked.
						@Override
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
		    	AlertDialog dialog = alertDialogBuilder.create();
		    	dialog.show();
			}
		});
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPos) {
		return entries.get(keys.get(groupPos)).size();
	}

	@Override
	public Object getGroup(int groupPos) {
		return keys.get(groupPos);
	}

	@Override
	public int getGroupCount() {
		return keys.size();
	}

	@Override
	public long getGroupId(int groupPos) {
		return groupPos;
	}

	@Override
	public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent) {
		String date = (String) getGroup(groupPos);
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.schedule_group_layout, null);
		}
		
		TextView dateView = (TextView) convertView.findViewById(R.id.schedule_date_view);
		dateView.setText(date);
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		return false;
	}

}
