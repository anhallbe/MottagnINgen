package nu.mottagningen.contact;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Scanner;

import nu.mottagningen.R;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;

/**
 * This activity displays contacts stored in the /assets/contacts.txt file.
 * @author Andreas
 *
 */
public class ContactActivity extends ListActivity {
	
	private ArrayList<Contact> contacts;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_contact);
		
		contacts = new ArrayList<Contact>();
		try {
			contacts = readContacts();
		} catch (IOException e) {
			e.printStackTrace();
			Toast.makeText(this, "Error", Toast.LENGTH_LONG).show();
			return;
		}
		
		ContactAdapter adapter = new ContactAdapter(this, contacts);
		setListAdapter(adapter);
	}

	/**
	 * Reads Contacts from a .txt-file.
	 * @return A list of Contacts.
	 * @throws java.io.IOException if the contacts.txt-file couldn't be read (didn't exist, badly formated, etc)
	 */
	private ArrayList<Contact> readContacts() throws IOException {
		ArrayList<Contact> list = new ArrayList<Contact>();
		InputStream in = getAssets().open("contacts.txt");
		Scanner s = new Scanner(in, "UTF-8");
		while(s.hasNextLine()) {
			String row = s.nextLine();
			String[] fields = row.split(",");
			if(fields.length == 3)
				list.add(new Contact(fields[0], fields[1], fields[2], null));
			else if(fields.length == 4)
				list.add(new Contact(fields[0], fields[1], fields[2], fields[3]));
		}
		s.close();
		return list;
	}
	
	/**
	 * Adapter used to display Contacts.
	 * @author Andreas
	 *
	 */
	private class ContactAdapter extends ArrayAdapter<Contact> {
		
		private ArrayList<Contact> contactList;
		private Context context;

		public ContactAdapter(Context context, ArrayList<Contact> list) {
			super(context, R.layout.contact_list_item, list);
			this.context = context;
			contactList = list;
		}
		
		@Override
		public View getView(int position, View contactView, ViewGroup parent) {
			if(contactView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				contactView = inflater.inflate(R.layout.contact_list_item, parent, false);
			}
			final Contact contact = contactList.get(position);
			
			TextView name = (TextView) contactView.findViewById(R.id.name);
			TextView title = (TextView) contactView.findViewById(R.id.title);
			ImageButton email = (ImageButton) contactView.findViewById(R.id.email);
			ImageButton phone = (ImageButton) contactView.findViewById(R.id.phone);
			
			name.setText(contact.getName());
			title.setText(contact.getTitle());
			
			if(contact.getEmail() == null)
				email.setVisibility(View.INVISIBLE);
			else {
				email.setVisibility(View.VISIBLE);
				email.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Toast.makeText(getApplicationContext(), contact.getEmail(), Toast.LENGTH_SHORT).show();
						Intent i = new Intent(Intent.ACTION_SEND);
						i.setType("plain/text");																						//When the "Email"-button is clicked, let
						i.putExtra(Intent.EXTRA_EMAIL, new String[] { contact.getEmail() });											//the user chose an email-client and construct a new email
						startActivity(Intent.createChooser(i, "Send email to " + contact.getName() + " (" + contact.getEmail() + ")"));	//with no subject, no content.
					}
				});
			}
			
			if(contact.getPhone() == null)
				phone.setVisibility(View.INVISIBLE);
			else {
				phone.setVisibility(View.VISIBLE);
				phone.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(Intent.ACTION_DIAL);
						i.setData(Uri.parse("tel:" + contact.getPhone()));													//When the "Call/Dial/Phone"-button is clicked,
						startActivity(i);																					//dial the Contact's number
						Toast.makeText(getApplicationContext(), "Dialing " + contact.getName(), Toast.LENGTH_LONG).show();
					}
				});
			}
			
			return contactView;
		}
	}
}
