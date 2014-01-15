package nu.mottagningen.news;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nu.mottagningen.R;

import org.mcsoxford.rss.RSSFeed;
import org.mcsoxford.rss.RSSItem;
import org.mcsoxford.rss.RSSReader;
import org.mcsoxford.rss.RSSReaderException;

import android.os.AsyncTask;
import android.os.Bundle;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * This activity will read news from a RSS-feed (specified by the Intent EXTRA_URL), and display them in a way similar to Google Now cards.
 * @author Andreas
 *
 */
public class NewsCardActivity extends ListActivity {
	
	private ArrayList<NewsCard> list = new ArrayList<NewsCard>();		//A list of news entries. This will be displayed to the user.
	private CardArrayAdapter adapter;									//Adapter used to display news cards.
	private ProgressBar progress;										//Displays the update-process.
	private String sourceURL;											//RSS-feed source
	
	public static final String EXTRA_URL = "nu.mottagningen.news.EXTRA_URL";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news);
		progress = (ProgressBar) findViewById(R.id.progress);
		
		Intent intent = getIntent();
		sourceURL = intent.getStringExtra(EXTRA_URL);
		
		adapter = new CardArrayAdapter(getApplicationContext(), list);
		setListAdapter(adapter);
		
		if(sourceURL != null)
			new FetchNewsTask().execute();		//Update and display the news from RSS-feed
		else
			Log.e("NewsCardActivity", "Could not load news. News source intent not specified.");
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.news_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if(item.getItemId() == R.id.action_update)
			new FetchNewsTask().execute();
		
		return super.onOptionsItemSelected(item);
	}

	/**
	 * This adapter will populate the card list with news titles and content.
	 * @author Andreas
	 *
	 */
	private class CardArrayAdapter extends ArrayAdapter<NewsCard> {
		
		private Context context;
		private ArrayList<NewsCard> cardList;
		
		public CardArrayAdapter(Context context, ArrayList<NewsCard> values) {
			super(context, R.layout.news_card_layout, values);
			this.context = context;
			this.cardList = values;
		}
		
		@Override
		public View getView(int position, View cardView, ViewGroup parent) {
			if(cardView == null) {
				LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				cardView = inflater.inflate(R.layout.news_card_layout, parent, false);
			}
			
			final NewsCard card = cardList.get(position);
			TextView titleView = (TextView) cardView.findViewById(R.id.title);
			TextView descriptionView = (TextView) cardView.findViewById(R.id.description);
			
			titleView.setText(card.getTitle());
			descriptionView.setText(card.getDescription());
			
			cardView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(context, WebNewsActivity.class);
					intent.putExtra(WebNewsActivity.TITLE_EXTRA, card.getTitle());
					intent.putExtra(WebNewsActivity.URL_EXTRA, card.getUrl());
					startActivity(intent);
				}
			});
			
			return cardView;
		}
	}
	
	/**
	 * This task will load info from the news feed and insert them in a {@link CardArrayAdapter}
	 * @author Andreas
	 *
	 */
	private class FetchNewsTask extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			try {
				RSSReader reader = new RSSReader();
				RSSFeed feed = reader.load(sourceURL);
				List<RSSItem> rsslist = feed.getItems();
				
				for(RSSItem i : rsslist) {
					NewsCard card = new NewsCard(i.getTitle(), Html.fromHtml(i.getDescription()).toString(), i.getLink().toString(), i.getPubDate());
					if(!list.contains(card))
						list.add(card);					
					Log.d("NewsCardActivity", "Added: " + i.getTitle() + "\nDescription: " + i.getDescription());
				}
				reader.close();
			} catch (RSSReaderException e) {
				Log.d("NewsCardActivity", "ERROR loading RSS");
				e.printStackTrace();
				return false;
			}
			
			return true;
		}
		
		@Override
		protected void onProgressUpdate(Void... values) {
			super.onProgressUpdate(values);
			adapter.notifyDataSetChanged();
		}
		
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progress.setVisibility(ProgressBar.VISIBLE);
		}
		
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			progress.setVisibility(ProgressBar.GONE);
			if(result) {
				Collections.sort(list);
				adapter.notifyDataSetChanged();
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_news_update_success), Toast.LENGTH_SHORT).show();
			}
			else
				Toast.makeText(getApplicationContext(), getResources().getString(R.string.message_connection_error), Toast.LENGTH_LONG).show();
		}
	}

}

