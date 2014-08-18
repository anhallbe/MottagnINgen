package nu.mottagningen.news;

import nu.mottagningen.R;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebNewsActivity extends Activity {
	
	private WebView webView;
	private String title;
	private String data;
	private String url;
	
	public static String DATA_EXTRA = "nu.mottagningen.DATA_EXTRA";
	public static String TITLE_EXTRA = "nu.mottagningen.TITLE_EXTRA";
	public static String URL_EXTRA = "nu.mottagningen.URL_EXTRA";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_web_news);
		
		Intent intent = getIntent();
		
		data = intent.getStringExtra(DATA_EXTRA);
		title = intent.getStringExtra(TITLE_EXTRA);
		url = intent.getStringExtra(URL_EXTRA);
		
		setTitle(title);
		
		webView = (WebView) findViewById(R.id.web_view);
		webView.setWebViewClient(new WebViewClient() {
			@Override
			public boolean shouldOverrideUrlLoading(WebView view, String url) {			//The view will handle loads itself....
				return false;
			}
		});
		
		if(url != null)
			webView.loadUrl(url);
		else
			webView.loadData(data, "text/html; charset=UTF-8", null);
	}

}
