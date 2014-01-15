package nu.mottagningen.news;

import java.util.Date;

/**
 * A NewsCard is a container for news-related information. Since it implements Comparable, NewsCards can be sorted and ordered by date.
 * @author Andreas
 *
 */
public class NewsCard implements Comparable<NewsCard> {
	private String title;
	private String description;
	private String content;
	private String url;
	private Date date;
	
	public NewsCard(String title, String description, String content, String url, Date date) {
		this.title = title;
		this.description = description;
		this.content = content;
		this.url = url;
		this.date = date;
	}
	
	public NewsCard(String title, String description, String url, Date date) {
		this.title = title;
		this.description = description;
		this.content = null;
		this.url = url;
		this.date = date;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}
	
	public Date getDate() {
		return date;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	@Override
	public boolean equals(Object o) {
		if(o instanceof NewsCard && title.equals(((NewsCard) o).getTitle()))	//If two cards have the same title, they're considered equal.
			return true;
		else
			return false;
	}

	@Override
	public int compareTo(NewsCard another) {
		return another.getDate().compareTo(date);		//Used to sort cards based on their publication dates.
	}
}
